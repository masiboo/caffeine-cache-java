package nl.ing.api.contacting.conf.business.survey

import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor.CallFlowRequestData
import nl.ing.api.contacting.util.Result
import nl.ing.api.contacting.util.exception.ContactingBusinessError
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz._

import java.time.Instant
import java.util.Date

/**
 * @author Ayush M
 */

class CallFlowQuartzScheduler(scheduler: Scheduler) extends LazyLogging {

  private val jobGroup: String = "callflow-surveys"
  private val minimumTriggerBefore: Long = 30

  def scheduleCallFlow(requestData: CallFlowRequestData, delayInSeconds: Long): Result[Unit] = {
    try {
      val jobDetail = buildJob(requestData)
      val jobTrigger = buildTrigger(requestData.cjid, delayInSeconds, jobDetail)
      scheduler.checkExists(jobDetail.getKey) match {
        case true => scheduler.clear()
        case false =>  scheduler.scheduleJob(jobDetail, jobTrigger)
      }
      logger.info(s"Scheduled survey callflow api call in ${delayInSeconds} seconds for  cjid ${requestData.cjid}")
      ().asRight[ContactingBusinessError]
    } catch {
      case sc: SchedulerException =>
        QuartzSchedulingFailed(requestData, sc).asLeft[Unit]
    }
  }

  private def buildJob(requestData: CallFlowRequestData): JobDetail = {
    val jobDataMap = new JobDataMap
    jobDataMap.put("accountFriendlyName", requestData.accountFriendlyName)
    jobDataMap.put("callFlowName", requestData.callFlowName)
    jobDataMap.put("cjid", requestData.cjid)
    jobDataMap.put("surveyNameInTheVoice", requestData.surveyNameInTheVoice)
    jobDataMap.put("surveySettingFriendlyName", requestData.surveySettingFriendlyName)
    jobDataMap.put("telephoneNumber", requestData.telephoneNumber)
    JobBuilder.newJob(classOf[CallFlowTriggerJob]).withIdentity(requestData.cjid, jobGroup).setJobData(jobDataMap).build
  }

  private def buildTrigger(cjid: String, delayInSeconds: Long, jobDetail: JobDetail) = {
    val scheduleIn = Math.max(delayInSeconds , minimumTriggerBefore)
    TriggerBuilder.newTrigger.withIdentity(cjid, jobGroup).startAt(Date.from(Instant.now().plusSeconds(scheduleIn))).forJob(jobDetail)
      .withSchedule(simpleSchedule().
        withMisfireHandlingInstructionIgnoreMisfires()
      )
      .build()
  }
}
