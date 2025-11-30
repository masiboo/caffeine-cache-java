package nl.ing.api.contacting.conf.business.survey

import cats.effect.IO
import com.twitter.finagle.http.Response
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.domain.SurveyCallRecordVO
import nl.ing.api.contacting.conf.support.TestData
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.Mockito.when
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime

/**
 * @author Ayush M
 */
class CallFlowResultProcessorSpec extends BaseSpec with TestData {

  val surveyStreamService: SurveyStreamService[IO] = Mockito.mock(classOf[SurveyStreamService[IO]])
  val testObject = new CallFlowResultProcessor(surveyStreamService)

  "for a success response and handling it" should "return correct result" in {
    val successResponse: Response = Response.apply(com.twitter.finagle.http.Status(200))
    val surveyCallRecordCaptor: ArgumentCaptor[SurveyCallRecordVO] = ArgumentCaptor.forClass(classOf[SurveyCallRecordVO])
    when(surveyStreamService.addOfferedSurveyCallRecord(surveyCallRecordCaptor.capture())).thenReturn(IO.pure(()))
    val res = testObject.handleResponse(successResponse, requestData).value.unsafeRunSync()
    res.isRight shouldBe true
  }

  "for a bad response it" should "return correct error code" in {
    val successResponse: Response = Response.apply(com.twitter.finagle.http.Status(403))
    val res = testObject.handleResponse(successResponse, requestData).value.unsafeRunSync()
    res.isLeft shouldBe true
    res.left.get.asInstanceOf[CallFlowNon2xxError].httpCode shouldBe 403
  }

  "for a success response and failed handling it" should "return correct error code" in {
    val successResponse: Response = Response.apply(com.twitter.finagle.http.Status(200))
    val surveyCallRecordCaptor: ArgumentCaptor[SurveyCallRecordVO] = ArgumentCaptor.forClass(classOf[SurveyCallRecordVO])
    when(surveyStreamService.addOfferedSurveyCallRecord(surveyCallRecordCaptor.capture())).thenReturn(IO.raiseError(new RuntimeException("error")))
    val res = testObject.handleResponse(successResponse, requestData).value.unsafeRunSync()
    res.isLeft shouldBe true
    res.left.get.asInstanceOf[AddingOfferedSurveyFailed].error.getMessage shouldBe "error"
  }
}
