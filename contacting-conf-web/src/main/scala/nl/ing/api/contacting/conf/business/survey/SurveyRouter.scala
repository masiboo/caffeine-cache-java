package nl.ing.api.contacting.conf.business.survey

import cats.effect.IO
import cats.implicits._
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor.CallFlowRequestData
import nl.ing.api.contacting.conf.business.survey.SurveyErrors.reportErrors
import nl.ing.api.contacting.util.syntaxf.FSyntax.FResultOps
/**
 * @author Ayush M
 */
class SurveyRouter(callFlowInMemoryScheduler: CallFlowInMemoryScheduler, callFlowQuartzScheduler: CallFlowQuartzScheduler) {

  def routeSurvey(requestData: CallFlowRequestData, delayInSeconds: Option[Long]): IO[Unit] = {
    val surveyCallFlowResult = if(delayInSeconds.isDefined && delayInSeconds.get > 30)
      callFlowQuartzScheduler.scheduleCallFlow(requestData,delayInSeconds.get).pure[IO].toResultT
    else
      callFlowInMemoryScheduler.scheduleCallFlow(requestData,delayInSeconds.getOrElse(0l))

    //all failures are reported only for now. In future we might add retries based on failure type
    surveyCallFlowResult.valueOr(reportErrors)
  }
}
