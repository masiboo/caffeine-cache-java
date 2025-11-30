package nl.ing.api.contacting.conf.business.survey

import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.support.TestData
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.quartz.JobBuilder
import org.quartz.{JobDetail, Scheduler, SchedulerException, Trigger}

import java.time.Instant
import java.util.Date

/**
 * @author Ayush M
 */
class CallFlowQuartzSchedulerSpec extends BaseSpec with TestData {


  "quartz scheduler" should "schedule job with correct data" in {
    val scheduler: Scheduler = Mockito.mock(classOf[Scheduler])
    val testObject = new CallFlowQuartzScheduler(scheduler)

    val jobDetailCaptor: ArgumentCaptor[JobDetail] = ArgumentCaptor.forClass(classOf[JobDetail])
    val jobTriggerCaptor: ArgumentCaptor[Trigger] = ArgumentCaptor.forClass(classOf[Trigger])
    val possibleStartTime = Date.from(Instant.now().plusSeconds(120l))
    when(scheduler.scheduleJob(jobDetailCaptor.capture(), jobTriggerCaptor.capture())).thenReturn(Date.from(Instant.now()))
    val res = testObject.scheduleCallFlow(requestData, 120l)
    res.isRight shouldBe true
    val jobDetail = jobDetailCaptor.getValue
    jobDetail.getKey.getGroup shouldBe "callflow-surveys"
    jobDetail.getKey.getName shouldBe requestData.cjid
    jobDetail.getJobClass.getName shouldBe "nl.ing.api.contacting.conf.business.survey.CallFlowTriggerJob"
    val jobDataMap = jobDetail.getJobDataMap
    jobDataMap.get("accountFriendlyName") shouldBe  requestData.accountFriendlyName
    jobDataMap.get("callFlowName")  shouldBe requestData.callFlowName
    jobDataMap.get("cjid") shouldBe requestData.cjid
    jobDataMap.get("surveyNameInTheVoice") shouldBe requestData.surveyNameInTheVoice
    jobDataMap.get("surveySettingFriendlyName") shouldBe requestData.surveySettingFriendlyName
    jobDataMap.get("telephoneNumber") shouldBe requestData.telephoneNumber

    val trigger = jobTriggerCaptor.getValue
    trigger.getStartTime.before(possibleStartTime) shouldBe false
  }


  "quartz scheduler" should "not trigger a job if already exist" in {
    val scheduler: Scheduler = Mockito.mock(classOf[Scheduler])
    val testObject = new CallFlowQuartzScheduler(scheduler)

    val job =  JobBuilder.newJob(classOf[CallFlowTriggerJob]).withIdentity(requestData.cjid, "callflow-surveys").build()
    when(scheduler.checkExists(job.getKey)).thenReturn(true)

    val res = testObject.scheduleCallFlow(requestData, 120l)
    res.isRight shouldBe true
    verify(scheduler, never()).scheduleJob(any(classOf[JobDetail]), any(classOf[Trigger]))
    verify(scheduler, times(1)).clear()

  }

  "quartz scheduler" should "return correct error code" in {
    val scheduler: Scheduler = Mockito.mock(classOf[Scheduler])
    val testObject = new CallFlowQuartzScheduler(scheduler)

    when(scheduler.scheduleJob(any(), any())).thenThrow(new SchedulerException("error"))
    val res = testObject.scheduleCallFlow(requestData, 120l)
    res.isLeft shouldBe true
    res.left.get.isInstanceOf[QuartzSchedulingFailed] shouldBe true
  }
}
