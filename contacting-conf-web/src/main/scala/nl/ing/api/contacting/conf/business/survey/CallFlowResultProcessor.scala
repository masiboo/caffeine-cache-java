package nl.ing.api.contacting.conf.business.survey

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.twitter.finagle.http.Response
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor.CallFlowRequestData
import nl.ing.api.contacting.conf.domain.SurveyCallRecordVO
import nl.ing.api.contacting.util.ResultT

import java.time.Instant
/**
 * @author Ayush M
 */
class CallFlowResultProcessor(surveyStreamService: SurveyStreamService[IO]) extends LazyLogging {


  def handleResponse(response: Response, requestData: CallFlowRequestData): ResultT[Unit] = EitherT({
    if(response.statusCode == 200) {
        for{
          res <- surveyStreamService.addOfferedSurveyCallRecord(SurveyCallRecordVO(requestData.accountFriendlyName,
            requestData.telephoneNumber, requestData.surveySettingFriendlyName, Instant.now())).attempt.map(_.leftMap(t => AddingOfferedSurveyFailed(requestData, t)))
        } yield res
      }
    else {
      IO.pure(CallFlowNon2xxError(requestData, response.statusCode, Option(response.status).map(_.reason).getOrElse("")).asLeft[Unit])
    }
  })
}
