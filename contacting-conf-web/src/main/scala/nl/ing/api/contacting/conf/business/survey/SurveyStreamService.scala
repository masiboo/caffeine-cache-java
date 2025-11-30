package nl.ing.api.contacting.conf.business.survey

import cats.effect.Async
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.business.BlacklistService
import nl.ing.api.contacting.conf.domain.{OfferableSurvey, SurveyCallRecordVO}
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.callflowSurveyExecutionContext
import nl.ing.api.contacting.conf.repository.SurveyRepository
import nl.ing.api.contacting.conf.repository.cassandra.SurveyCallRecordsRepository
import nl.ing.api.contacting.conf.repository.model.{SurveyPhoneNumberFormat, SurveySetting, SurveySettingOptions}
import nl.ing.api.contacting.conf.surveytrigger.{ContactingSurveyTriggerEvent, SelfServiceSurveyTriggerEvent, SurveyEvent}
import nl.ing.api.contacting.tracing.Trace
import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class SurveyStreamService[F[_]: Async: Trace](surveyCallRecordsRepository: SurveyCallRecordsRepository[F],
                                              surveyRepository: SurveyRepository[F], randomizer: Randomizer, blacklistService: BlacklistService[F]) extends LazyLogging{
  import nl.ing.api.contacting.conf.domain.SurveyConstants._

  implicit val executionContext: ExecutionContext = callflowSurveyExecutionContext

  val aDayInMillis = 24 * 60 * 60 * 1000

  def processIfSurveyEligible(event: SurveyEvent): F[Option[OfferableSurvey]] = {
    for{
      isBlocked <- isPhoneNumberInBlocklist(event)
      offerable <- if(isBlocked){
        logger.warn(s"The customer phone number is blocked, for cjid: ${event.cjid}, no survey will be offered")
        Async[F].pure(None)
      } else {
        event match {
          case contactingSurveyEvent: ContactingSurveyTriggerEvent =>  checkSettings(contactingSurveyEvent)
          case selfServiceSurveyEvent: SelfServiceSurveyTriggerEvent =>  checkSettingsForSelfServiceSurveyEvent(selfServiceSurveyEvent)
        }

      }
    } yield offerable
  }

  private def checkSettingsForSelfServiceSurveyEvent(event: SelfServiceSurveyTriggerEvent): F[Option[OfferableSurvey]]  = {
    for{
      settings <- surveyRepository.getSurveySettingsByName(event.surveyName, event.accountSid)
        .map(filterBasedOnCriteria(event))
      settingsAfterMinFreq <- checkMinFrequency(settings)
    } yield settingsAfterMinFreq
  }

  private def checkSettings(callDetailVO: ContactingSurveyTriggerEvent): F[Option[OfferableSurvey]] = {
    for{
      settings <- if(callDetailVO.callDirection equalsIgnoreCase CHANNEL_DIRECTION_INBOUND){
        getIfSurveyEligibleForInbound(callDetailVO).map{filterBasedOnCriteria(callDetailVO)}
      } else {
        getIfSurveyEligibleForOutbound(callDetailVO).map(filterBasedOnCriteria(callDetailVO))
      }
      _ = if (settings.isEmpty) logger.info(s"Survey not eligible for cjid: ${callDetailVO.cjid} with callSid ${callDetailVO.callSid} because ")
      settingsAfterMinFreq <- checkMinFrequency(settings)
      _ = if (settingsAfterMinFreq.isEmpty) logger.info(s"Survey not eligible for cjid: ${callDetailVO.cjid} with callSid ${callDetailVO.callSid} because exceed the minimum frequency")
    } yield settingsAfterMinFreq
  }

  private def isPhoneNumberInBlocklist(surveyEvent: SurveyEvent): F[Boolean] = {
    for{
      blacklistItems <- blacklistService.getAllBlacklistItems(surveyEvent.accountSid)
      result = blacklistItems.exists(_.value == surveyEvent.customerPhoneNumber)
    } yield result
  }

  private def checkMinFrequency(surveyDetailOpt: Option[OfferableSurvey]): F[Option[OfferableSurvey]] = {
    (surveyDetailOpt match {
      case Some(surveyDetail) =>
        surveyDetail.setting.minFrequency match {
          case Some(minFreq) =>
            surveyCallRecordsRepository.findCallRecordsForCustomerAndSurvey(surveyDetail.account.friendlyName, surveyDetail.callDetail.customerPhoneNumber, surveyDetail.setting.name).map {
              case Nil => Option(surveyDetail)
              case recordVO :: _ if (System.currentTimeMillis() - recordVO.offered_datetime.toEpochMilli) / aDayInMillis > minFreq => Option(surveyDetail)
              case _ => None
            }
          case None => Future.successful(Option(surveyDetail)).asDelayedF
        }
      case None => Future.successful[Option[OfferableSurvey]](None).asDelayedF
    })
  }

  private def filterBasedOnCriteria(event: SurveyEvent)(options: SurveySettingOptions): Option[OfferableSurvey] = {
    import options._
    val matchedCriteria = event match {
      case contactingSurveyTriggerEvent: ContactingSurveyTriggerEvent =>
        setting.exists(settings => isEnoughDuration(settings, contactingSurveyTriggerEvent.callDuration, event.cjid, event.callSid) &&
          isCallflowNameDefined(settings, event.cjid, event.callSid) &&
          isWithinPercentage(settings, event.cjid, event.callSid) &&
          isTransferCallButCallflowNotForTransfer(settings, contactingSurveyTriggerEvent.transferIndicator, event.cjid, event.callSid) &&
          isPhoneNumberEligible(contactingSurveyTriggerEvent.customerPhoneNumber, phFormats, event.cjid, event.callSid))
      case _: SelfServiceSurveyTriggerEvent =>
        setting.exists(settings => isEnoughDuration(settings, event.callDuration, event.cjid, event.callSid) &&
          isCallflowNameDefined(settings, event.cjid, event.callSid) &&
          isWithinPercentage(settings, event.cjid, event.callSid) &&
          isPhoneNumberEligible(event.customerPhoneNumber, phFormats, event.cjid, event.callSid))
    }
    if(matchedCriteria) {
      for{
        acc <- account
        sett <- setting
      } yield OfferableSurvey(event, acc, sett)
    } else {
      logger.info(s"Survey not eligible for cjid: ${event.cjid} with callSid ${event.callSid} because filter out based on criteria: $setting")
      None
    }
  }

  private def isEnoughDuration(setting: SurveySetting, callDuration: Int, cjid: String, callSid: String) : Boolean = {
    val isEnough = setting.minContactLength.forall(_ <= callDuration)
    if (!isEnough) logger.info(s"Survey not eligible for cjid: ${cjid} with callSid ${callSid} because filter out based on not enough duration")
    isEnough
  }

  private def isCallflowNameDefined(setting: SurveySetting, cjid: String, callSid: String) : Boolean = {
    val isCallflowNameDefined = setting.callflowName.isDefined
    if(!isCallflowNameDefined) logger.info(s"Survey not eligible for cjid: ${cjid} with callSid ${callSid} because filter out based on callflow name not defined")
    else logger.info(s"Potential survey eligible for cjid: ${cjid} with callSid ${callSid} callflow: ${setting.callflowName}")
    isCallflowNameDefined
  }
  private def isWithinPercentage(settings: SurveySetting, cjid: String, callSid: String): Boolean = {
    val randomPercentage = randomizer.random(100)
    val isWithinPercentage = settings.surveyOfferRatio.forall(pct => pct >= randomPercentage)
    if(!isWithinPercentage) logger.info(s"Survey not eligible for cjid: ${cjid} with callSid ${callSid} because filter out based on below percentage ratio")
    isWithinPercentage
  }

  private def isTransferCallButCallflowNotForTransfer(settings: SurveySetting, transferIndicator: Boolean, cjid: String, callSid: String): Boolean = {
    val isTransferCallButCallflowNotForTransfer = !transferIndicator || (settings.surveyForTransfers && transferIndicator)
    if(!isTransferCallButCallflowNotForTransfer) logger.info(s"Survey not eligible for cjid: ${cjid} with callSid ${callSid} because filter out based on transfer call but callflow is not for transfer")
    isTransferCallButCallflowNotForTransfer
  }

  def isPhoneNumberEligible(phone: String, phFormats: List[SurveyPhoneNumberFormat], cjid: String, callSid: String) = {
    val (bannedPhones, allowedPhones)= phFormats.partition(_.direction)
    val isAllowed = allowedPhones.exists(fmt => stringToRegex(fmt.format).findFirstMatchIn(phone).nonEmpty)
    val isNotAllowed = !bannedPhones.exists(fmt => stringToRegex(fmt.format).findFirstMatchIn(phone).nonEmpty)
    if (!isAllowed) logger.info(s"Survey not eligible for cjid: ${cjid} with callSid ${callSid} because filter out based on not allowed phone number")
    if (!isNotAllowed) logger.info(s"Survey not eligible for cjid: ${cjid} with callSid ${callSid} because filter out based on banned phone number")
    val isFinallyEligible = isAllowed && isNotAllowed
    isFinallyEligible
  }

  private def stringToRegex(fmt: String) = {
    if(fmt.contains('*')){
      s"^\\$fmt".r
    } else {
      val fmtEnding = fmt + "$"
      s"^\\$fmtEnding".r
    }
  }


  def getIfSurveyEligibleForInbound(callDetail: ContactingSurveyTriggerEvent): F[SurveySettingOptions] = {
      surveyRepository.getIfSurveyEligibleForInbound(callDetail)
  }

  def getIfSurveyEligibleForOutbound(callDetail: ContactingSurveyTriggerEvent): F[SurveySettingOptions] = {
      surveyRepository.getIfSurveyEligibleForOutbound(callDetail)
  }

  def addOfferedSurveyCallRecord(surveyCallRecordVO: SurveyCallRecordVO): F[Unit] = {
    surveyCallRecordsRepository.upsert(surveyCallRecordVO)
  }

}

class Randomizer{

  def random(upperLimit: Int): Int = Random.nextInt(upperLimit)

}
