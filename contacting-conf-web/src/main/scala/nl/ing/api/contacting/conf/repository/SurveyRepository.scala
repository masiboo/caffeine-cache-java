package nl.ing.api.contacting.conf.repository

import cats.Monad
import cats.Parallel
import cats.effect.Sync
import cats.implicits._
import com.ing.api.contacting.dto.context.ContactingContext
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.exception.ValueMissing
import nl.ing.api.contacting.conf.repository.model.SurveyDetails
import nl.ing.api.contacting.conf.repository.model.SurveyPhoneNumberFormat
import nl.ing.api.contacting.conf.repository.model.SurveySetting
import nl.ing.api.contacting.conf.repository.model.SurveySettingOptions
import nl.ing.api.contacting.conf.surveytrigger.ContactingSurveyTriggerEvent
import nl.ing.api.contacting.tracing.Trace
import nl.ing.api.contacting.util.ResultF
import nl.ing.api.contacting.util.syntaxf.FSyntax.FOps
import nl.ing.api.contacting.util.syntaxf.FSyntax.FOptionOps

import scala.language.higherKinds

/**
 * @author Ayush Mittal
 */

/**
 * A class to CRUD on survey settings and related tables
 * @param surveySettingRepo
 * @param surveyPhNumberFormatRepo
 * @param surveyTaskQRepository
 * @param surveyOrgRepository
 * @tparam F
 */
class SurveyRepository[F[_]: Sync: Trace](val surveySettingRepo: SurveySettingRepository[F], val surveyPhNumberFormatRepo: SurveyPhNumberFormatRepo[F],
                             val surveyTaskQRepository: SurveyTaskQRepository[F], val surveyOrgRepository: SurveyOrgRepository[F]) extends LazyLogging {

  /**
   * Get survey settings and related info
   * @param surveyId : Survey Id
   * @param context: contacting context
   * @param parallel : implicit instance of cats parallel to fetch data from detail tables
   * @return SurveyDetails if found else None
   */
  def getSurveyDetails(surveyId: Long, context: ContactingContext)(implicit parallel: Parallel[F]): F[Option[SurveyDetails]] = {
    implicit val monad: Monad[F] = parallel.monad
    for {
      surveySetttings <- surveySettingRepo.findById(surveyId, context)
      details <-
        if (surveySetttings.isDefined) {
          (surveyPhNumberFormatRepo.getAllBySurveyId(surveyId), surveyTaskQRepository.getAllBySurveyId(surveyId), surveyOrgRepository.getAllBySurveyId(surveyId)).parMapN {
            case (a, b, c) =>
              Some(SurveyDetails(surveySetttings.get, a, b, c))
          }
        }
        else {
          Monad[F].pure(None)
        }
    } yield details
  }

  /**
   *
   * @param surveyId: Survey id
   * @param context: contacting context
   * @return : SurveySettings
   */
  def getSurveySettings(surveyId: Long, context: ContactingContext): F[Option[SurveySetting]] =
    surveySettingRepo.findById(surveyId, context)

  /**
   *
   * @param surveyName: Survey name
   * @param accountSid: account sid
   * @return : SurveySettingOptions
   */
  def getSurveySettingsByName(surveyName: String, accountSid: String): F[SurveySettingOptions] = {
    surveySettingRepo.findByName(surveyName, accountSid)
  }

  /**
   *
   * @param context: contacting context
   * @return : SurveySettings
   */
  def getSurveySettings(context: ContactingContext): F[Seq[SurveySetting]] =
    surveySettingRepo.findAll(context)

  def getIfSurveyEligibleForInbound(callDetailVO: ContactingSurveyTriggerEvent): F[SurveySettingOptions] = for {
    surveySettingOptions <- surveySettingRepo.getIfSurveyEligibleForInbound(callDetailVO)
    _ = if (surveySettingOptions.setting.isEmpty) logger.info((s"No eligible survey found inbound query for cjid ${callDetailVO.cjid}, tq sid ${callDetailVO.taskQueueSid}, worker sid ${callDetailVO.workerSid}" ))
    }
    yield(surveySettingOptions)

  def getIfSurveyEligibleForOutbound(callDetailVO: ContactingSurveyTriggerEvent): F[SurveySettingOptions] = for {
    surveySettingOptions <- surveySettingRepo.getIfSurveyEligibleForOutbound(callDetailVO)
    _ = if (surveySettingOptions.setting.isEmpty) logger.info((s"No eligible survey found outbound query for cjid ${callDetailVO.cjid}, tq sid ${callDetailVO.taskQueueSid}, worker sid ${callDetailVO.workerSid}"))
  }
  yield(surveySettingOptions)

  /**
   * Add, remove connected orgs
   */
  def addRemoveOrgs(surveyId: Long, orgsToAdd: List[Long], orgsToRemove: List[Long]): F[List[Any]] = {
    surveyOrgRepository.addRemoveOrgs(surveyId, orgsToAdd, orgsToRemove)
  }

  /**
   * Add, remove connected taskqueues
   */
  def addRemoveTaskqueues(surveyId: Long, taskQueuesToAdd: List[Long], taskQueuesToRemove: List[Long]): F[List[Any]] = {
    surveyTaskQRepository.addRemoveTaskQueues(surveyId, taskQueuesToAdd, taskQueuesToRemove)
  }

  /**
   *
   * @param model
   * @param formatsAdded
   * @param formatsRemoved
   * @param context
   * @return
   */
  def updateSurveySettings(model: SurveySetting, formatsAdded: Seq[SurveyPhoneNumberFormat], formatsRemoved: Seq[SurveyPhoneNumberFormat], context: ContactingContext): ResultF[F, Unit] = {
    for {
      _ <- surveySettingRepo.findById(model.id.getOrElse(-1l), context).ifEmpty(ValueMissing(s"Survey with id ${model.id} not found"))
      _ <- surveySettingRepo.update(model, context).adaptErrorT()
      _ <- surveyPhNumberFormatRepo.addDeleteFormats(formatsAdded, formatsRemoved).adaptErrorT()
    } yield ()
  }

  /**
   * Remove a survey setting
   */
  def removeSurveySetting(surveyId: Long, context: ContactingContext): F[Int] = {
    surveySettingRepo.deleteById(surveyId, context)
  }
}

