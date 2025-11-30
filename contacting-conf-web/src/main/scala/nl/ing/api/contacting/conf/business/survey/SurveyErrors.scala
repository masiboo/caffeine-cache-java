package nl.ing.api.contacting.conf.business.survey

import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor.CallFlowRequestData
import nl.ing.api.contacting.util.exception.ContactingBusinessError


/**
 * @author Ayush M
 */
sealed trait SurveyErrors extends ContactingBusinessError

/**
 * The quartz schedulder was not able to add a schedule to trigger the callflow
 * @param requestData
 */
case class QuartzSchedulingFailed(requestDate: CallFlowRequestData, error: Throwable) extends SurveyErrors

/**
 * CallFlow Client was not able to fire a http request
 * @param requestData
 */
case class CallFlowTriggerFailure(requestDate: CallFlowRequestData, error: Throwable) extends SurveyErrors

/**
 * Callflow api returned a non 2xx response code
 * @param requestData
 * @param httpCode
 * @param msg
 */
case class CallFlowNon2xxError(requestDate: CallFlowRequestData, httpCode: Int, msg: String) extends SurveyErrors

/**
 * After receiving a succesfull 2xx from callflow api, the coco api was not able to
 * add the offered call in database
 * @param requestData
 */
case class AddingOfferedSurveyFailed(requestDate: CallFlowRequestData, error: Throwable) extends SurveyErrors

object SurveyErrors extends LazyLogging {

  def reportErrors(contactingBusinessError: ContactingBusinessError): Unit = {
    contactingBusinessError match {
      case AddingOfferedSurveyFailed(rd, error) =>
        logger.error(s"error while adding offered survey callflow to database for survey request $rd", error)
      case CallFlowNon2xxError(rd, httpCode, msg) =>
        logger.error(s"The call to callflow was unsuccessful for survey request $rd,  code: $httpCode msg: $msg")
      case CallFlowTriggerFailure(rd, error) =>
        logger.error(s"Unable to trigger callflow api for survey request $rd", error)
      case QuartzSchedulingFailed(rd, error) =>
        logger.error(s"error while scheduling survey callflow api call for survey request $rd", error)
      case other =>
        logger.error(s"failed in callflow survey $other")
    }
  }
}