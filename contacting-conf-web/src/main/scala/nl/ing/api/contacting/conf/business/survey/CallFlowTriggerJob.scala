package nl.ing.api.contacting.conf.business.survey

import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor.{CallFlowRequestData, maximumWaitForCallflowInJob}
import nl.ing.api.contacting.conf.business.survey.CallFlowTriggerJob._
import nl.ing.api.contacting.conf.business.survey.SurveyErrors.reportErrors
import nl.ing.api.contacting.conf.modules.{CoreModule, ExecutionContextConfig}
import org.quartz.{Job, JobDataMap, JobExecutionContext}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

import scala.concurrent.Await
/**
 * @author Ayush M
 */

object CallFlowTriggerJob {
  val jobDataMapExpectedKeys = List("accountFriendlyName", "callFlowName","cjid","surveyNameInTheVoice", "surveySettingFriendlyName", "telephoneNumber")

  def fetchRequestData(dataMap: JobDataMap): Either[String, CallFlowRequestData] =  {
    jobDataMapExpectedKeys.map(dataMap.getString) match {
      case List(a: String, b: String, c: String, d: String, e: String, f: String)  =>
        Right(CallFlowRequestData(a,b,c,d,e,f))
      case _ => Left("unable to fetch callflow request data from job data map")
    }
  }
}

class CallFlowTriggerJob extends Job with LazyLogging {

  @Autowired
  @Lazy
  private var systemModule: CoreModule = _

  override def execute(context: JobExecutionContext): Unit = {
    val dataMap: JobDataMap = context.getJobDetail.getJobDataMap
    runJob(dataMap, context.getJobDetail.getKey.getName)
  }

  def runJob(dataMap: JobDataMap, jobName: String): Unit = {
    fetchRequestData(dataMap) match {
      case Left(error) =>
        logger.error(s"$error - for job : $jobName")
      case Right(requestData) =>
        // an await here because this is running in the thread pool of quartz job
        // and we need to make sure one job finishes before the next job runs
        // if there is delay on callflow, the processing is also delayed
        Await.result(systemModule.callFlowTriggerAndProcessor.execute(requestData).valueOr(reportErrors).unsafeToFuture()(ExecutionContextConfig.ioRunTime), maximumWaitForCallflowInJob)
    }
  }

}
