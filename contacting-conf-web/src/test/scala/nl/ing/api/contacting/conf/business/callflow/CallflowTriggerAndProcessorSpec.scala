package nl.ing.api.contacting.conf.business.callflow

import cats.data.EitherT
import cats.effect.IO
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import io.opentelemetry.api.trace.Tracer
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.business.survey.{CallFlowNon2xxError, CallFlowResultProcessor, CallFlowTriggerFailure}
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime
import nl.ing.api.contacting.conf.support.TestData
import nl.ing.api.contacting.util.ResultT
import nl.ing.api.contacting.util.exception.ContactingBusinessError
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, Mockito}

class CallflowTriggerAndProcessorSpec extends BaseSpec  with TestData {

  val service = mock[Service[Request, Response]]
  val tracer: Tracer = Mockito.mock(classOf[Tracer])
  val resultProcessor: CallFlowResultProcessor = Mockito.mock(classOf[CallFlowResultProcessor])
  val successResponse: Response = Response.apply(com.twitter.finagle.http.Status(200))

  val testObject = new CallFlowTriggerAndProcessor(service, tracer, resultProcessor)

  "A successful trigger and process" should "return correct result" in {
    val requestCaptor: ArgumentCaptor[Request] = ArgumentCaptor.forClass(classOf[Request])
    when(service.apply(requestCaptor.capture())).thenReturn(com.twitter.util.Future.apply(successResponse))
    when(resultProcessor.handleResponse(successResponse, requestData)).thenReturn(EitherT.rightT[IO,ContactingBusinessError](()))
    val res = testObject.execute(requestData)
    res.value.unsafeRunSync().isRight shouldBe true
    val request = requestCaptor.getValue
    request.headerMap.get("VND.ING.EXT.COUNTRY") shouldBe Some("NL")
    request.contentString shouldBe "{\"telephoneNumber\":\"0620787\",\"surveyNameInTheVoice\":\"voiceSurvey\",\"surveyCJID\":\"1234-cjid\"}"
    request.method.name shouldBe "POST"
    request.uri shouldBe "/contacting-callflows/outbound-calls/zapping"
  }

  "A successful trigger and failed processing" should "return correct error code" in {
    val requestCaptor: ArgumentCaptor[Request] = ArgumentCaptor.forClass(classOf[Request])
    when(service.apply(requestCaptor.capture())).thenReturn(com.twitter.util.Future.apply(successResponse))
    val error: ResultT[Unit] = EitherT.leftT[IO,Unit](CallFlowNon2xxError(requestData, 403, "error"))
    when(resultProcessor.handleResponse(successResponse, requestData)).thenReturn(error)
    val res = testObject.execute(requestData).value.unsafeRunSync()
    res.isLeft shouldBe true
    res.left.get.asInstanceOf[CallFlowNon2xxError].httpCode shouldBe 403
    val request = requestCaptor.getValue
    request.headerMap.get("VND.ING.EXT.COUNTRY") shouldBe Some("NL")
    request.contentString shouldBe "{\"telephoneNumber\":\"0620787\",\"surveyNameInTheVoice\":\"voiceSurvey\",\"surveyCJID\":\"1234-cjid\"}"
    request.method.name shouldBe "POST"
    request.uri shouldBe "/contacting-callflows/outbound-calls/zapping"
  }

  "A failed api call" should "return correct error code" in {
    val requestCaptor: ArgumentCaptor[Request] = ArgumentCaptor.forClass(classOf[Request])
    when(service.apply(requestCaptor.capture())).thenThrow(new RuntimeException("error"))
    val res = testObject.execute(requestData).value.unsafeRunSync()
    res.isLeft shouldBe true
    res.left.get.asInstanceOf[CallFlowTriggerFailure].error.getMessage shouldBe "error"
    val request = requestCaptor.getValue
    request.headerMap.get("VND.ING.EXT.COUNTRY") shouldBe Some("NL")
    request.contentString shouldBe "{\"telephoneNumber\":\"0620787\",\"surveyNameInTheVoice\":\"voiceSurvey\",\"surveyCJID\":\"1234-cjid\"}"
    request.method.name shouldBe "POST"
    request.uri shouldBe "/contacting-callflows/outbound-calls/zapping"
  }
}
