package nl.ing.api.contacting.conf.business.survey

import cats.data.EitherT
import cats.effect.IO
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor
import nl.ing.api.contacting.conf.support.TestData
import nl.ing.api.contacting.util.exception.ContactingBusinessError
import org.mockito.Mockito
import org.mockito.Mockito.when
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime
/**
 * @author Ayush M
 */
class CallFlowInMemorySchedulerSpec extends BaseSpec with TestData{

  val callFlowTriggerAndProcessor: CallFlowTriggerAndProcessor = Mockito.mock(classOf[CallFlowTriggerAndProcessor])
  val testObject : CallFlowInMemoryScheduler = new CallFlowInMemoryScheduler(callFlowTriggerAndProcessor)

  "in memory scheduler" should "delay before call" in {
    when(callFlowTriggerAndProcessor.execute(requestData)).thenReturn(EitherT.rightT[IO,ContactingBusinessError](()))
    val res = testObject.scheduleCallFlow(requestData, 5l)
    res.value.unsafeRunSync().isRight shouldBe true
  }
}
