package nl.ing.api.contacting.conf.business.survey

import cats.data.EitherT
import cats.effect.IO
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor.CallFlowRequestData
import nl.ing.api.contacting.conf.modules.CoreModule
import nl.ing.api.contacting.test.mock.MockitoSupport
import nl.ing.api.contacting.util.exception.ContactingBusinessError
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import org.scalatest.concurrent.ScalaFutures
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.Mockito.when
import org.quartz.JobDataMap

/**
 * @author Ayush M
 */
class CallFlowTriggerJobSpec extends MockitoSupport[CallFlowTriggerJob] with BeforeAndAfter with ScalaFutures with BeforeAndAfterEach{

  val callFlowTriggerAndProcessor : CallFlowTriggerAndProcessor = Mockito.mock(classOf[CallFlowTriggerAndProcessor])
  override def beforeEach(): Unit = {
    when(get[CoreModule].callFlowTriggerAndProcessor).thenReturn(callFlowTriggerAndProcessor)
  }

  "job" should "be triggerd" in {
    val jobDataMap: JobDataMap = new JobDataMap()
    jobDataMap.put("accountFriendlyName", "NL-unit")
    jobDataMap.put("callFlowName", "cf-name")
    jobDataMap.put("cjid", "cjid")
    jobDataMap.put("surveyNameInTheVoice", "voicename")
    jobDataMap.put("surveySettingFriendlyName", "settingname")
    jobDataMap.put("telephoneNumber", "+31888839393")

    val dataCaptor : ArgumentCaptor[CallFlowRequestData] = ArgumentCaptor.forClass(classOf[CallFlowRequestData])
    when(callFlowTriggerAndProcessor.execute(dataCaptor.capture())).thenReturn(EitherT.right[ContactingBusinessError](IO.pure(())))
    testObject.runJob(jobDataMap, "surveyjob")
    dataCaptor.getValue.cjid shouldBe "cjid"
    dataCaptor.getValue.callFlowName shouldBe "cf-name"
    dataCaptor.getValue.accountFriendlyName shouldBe "NL-unit"
    dataCaptor.getValue.surveyNameInTheVoice shouldBe "voicename"
    dataCaptor.getValue.surveySettingFriendlyName shouldBe "settingname"
    dataCaptor.getValue.telephoneNumber shouldBe "+31888839393"
  }
}
