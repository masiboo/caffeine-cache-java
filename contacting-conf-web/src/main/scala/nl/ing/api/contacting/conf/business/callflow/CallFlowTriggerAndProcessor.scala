package nl.ing.api.contacting.conf.business.callflow

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.ing.apisdk.toolkit.connectivity.transport.http.japi.RichHttpRequestBuilder
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request, Response}
import com.typesafe.scalalogging.LazyLogging
import io.opentelemetry.api.trace.{SpanContext, TraceFlags, TraceState, Tracer}
import nl.ing.api.contacting.conf.FutureUtils.RichTwitterFuture
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor._
import nl.ing.api.contacting.conf.business.survey.{CallFlowResultProcessor, CallFlowTriggerFailure}
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig
import nl.ing.api.contacting.tracing.TracingOps._
import nl.ing.api.contacting.util.ResultT
import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * @author Ayush M
 */
object CallFlowTriggerAndProcessor {

  val callflowUrl = "https://api.ing.com/contacting-callflows/outbound-calls"
  val SUBACCOUNT_HEADER_NAME = "VND.ING.EXT.COUNTRY"

  case class CallFlowRequestData(accountFriendlyName: String,
                                 callFlowName: String,
                                 cjid: String,
                                 surveyNameInTheVoice: String,
                                 surveySettingFriendlyName: String,
                                 telephoneNumber: String) {
    override def toString: String = s"Survey name $surveySettingFriendlyName callflow $callFlowName cjid $cjid account $accountFriendlyName"
  }

  private case class CallFlowRequestDataDto(telephoneNumber: String,
                                            surveyNameInTheVoice: String,
                                            surveyCJID: String)

  val maximumWaitForCallflowInJob = 4.seconds
}

class CallFlowTriggerAndProcessor(service: Service[Request, Response], tracer: Tracer, resultProcessor: CallFlowResultProcessor) extends LazyLogging {

  implicit val executionContext: ExecutionContext = ExecutionContextConfig.ioExecutionContext

  def execute(requestData: CallFlowRequestData): ResultT[Unit] = {
    for {
      response <- EitherT(doRequest(requestData).asDelayedF[IO].attempt.map(_.leftMap(t => CallFlowTriggerFailure(requestData, t))))
      saveOfferedSurvey <- resultProcessor.handleResponse(response, requestData)
    } yield saveOfferedSurvey
  }

  private def doRequest(requestData: CallFlowRequestData): Future[Response] = {
    val span = Try(newINGSpan("coco-callflow-trigger", requestData.cjid.replace("-", ""))(tracer))
    span.map(_.setAttribute("surveySetting", requestData.surveySettingFriendlyName))
    span.map(_.setAttribute("surveyNameInTheVoice", requestData.surveyNameInTheVoice))
    val request = requestBuilder(requestData.telephoneNumber, requestData.callFlowName, requestData.surveyNameInTheVoice,
      requestData.cjid, requestData.accountFriendlyName)
    val scope = span.map(_.makeCurrent())
    service.apply(request).asScala.andThen{
      case _ => scope.foreach(_.close())
    }
  }


  private def requestBuilder(telephoneNumber: String, callFlowName: String, surveyNameInTheVoice: String, cjid: String, accountFriendlyName: String): Request = {
    val subAccountHeader = Map(SUBACCOUNT_HEADER_NAME -> accountFriendlyName.split("-").headOption.getOrElse(accountFriendlyName))
    new RichHttpRequestBuilder()
      .withMethod(Method.Post)
      .withUrl(url = callflowUrl + s"/$callFlowName")
      .withHeaders(subAccountHeader)
      .withJsonContent(CallFlowRequestDataDto(telephoneNumber, surveyNameInTheVoice, cjid))
      .build
  }
}
