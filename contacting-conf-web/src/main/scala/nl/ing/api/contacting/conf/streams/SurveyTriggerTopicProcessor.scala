package nl.ing.api.contacting.conf.streams

import cats.effect.{Async, IO, LiftIO}
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor.CallFlowRequestData
import nl.ing.api.contacting.conf.business.survey.{SurveyRouter, SurveyStreamService}
import nl.ing.api.contacting.conf.domain.OfferableSurvey
import nl.ing.api.contacting.conf.surveytrigger.{ContactingSurveyTriggerEvent, SelfServiceSurveyTriggerEvent, SurveyEvent, SurveyTriggerEventProcessor}

class SurveyTriggerTopicProcessor[F[_]: Async: LiftIO](surveyStreamService: SurveyStreamService[F], surveyRouter: SurveyRouter) extends LazyLogging with SurveyTriggerEventProcessor[F]{
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  def processEvent(event: SurveyEvent): F[Unit] = {
    logger.info(s"call detail found for cjid: ${event.cjid} , proceeding to find a matching survey setting with event: $event")
    for {
      optSurveyStream <- surveyStreamService.processIfSurveyEligible(event)
      _ <- optSurveyStream match {
        case Some(value) =>
          logger.info(s"survey details found for cjid: ${event.cjid}, proceeding to trigger callflow: $value")
          triggerCallFlow(event, value)
        case None =>
          event match {
            case contactingSurveyEvent: ContactingSurveyTriggerEvent =>
              logger.info(s"No eligible survey found for cjid ${contactingSurveyEvent.cjid}, tq sid ${contactingSurveyEvent.taskQueueSid}, worker sid ${contactingSurveyEvent.workerSid}")
            case selfServiceSurveyEvent: SelfServiceSurveyTriggerEvent =>
              logger.info(s"No eligible survey found for cjid ${selfServiceSurveyEvent.cjid}, callSid ${selfServiceSurveyEvent.callSid}")
          }
          IO.unit.to[F]
      }
    } yield ()

  }

  private def triggerCallFlow(event: SurveyEvent, survey: OfferableSurvey): F[Unit] = {
    import survey._
    LiftIO[F].liftIO(surveyRouter.routeSurvey(CallFlowRequestData(accountFriendlyName = account.friendlyName, callFlowName = setting.callflowName.getOrElse(""), cjid = event.cjid, surveyNameInTheVoice = survey.setting.voiceSurveyId, surveySettingFriendlyName = survey.setting.name, telephoneNumber = event.customerPhoneNumber), survey.setting.delay))
  }

}
