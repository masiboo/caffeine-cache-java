package nl.ing.api.contacting.conf.util

import com.twilio.exception.ApiException
import com.typesafe.scalalogging.LazyLogging
import jakarta.ws.rs.core.{MediaType, Response}
import org.apache.commons.lang3.StringUtils

/**
  * Created by Piet Schrijver on 14-1-2016.
  */
object TwilioRestExceptionUtil extends LazyLogging {
  val NOT_FOUND = 20404
  val BAD_REQUEST = 21402
  /*
      invalid json, unknown parameters and invalid task queue while creating workflow
      For all above error cases twilio response code is 20001.
      We will add more error codes as we figure them out.
   */
  val UNKNOWN_PARAMETERS = 20001

  def exceptionToResponse(e: ApiException): Response = {
    if (e.getCode == null)
      return Response.status(Response.Status.BAD_REQUEST).`type`(MediaType.TEXT_PLAIN).entity(e.getMessage).build
    e.getCode.toInt match {
      case NOT_FOUND =>
        Response.status(Response.Status.NOT_FOUND).`type`(MediaType.TEXT_PLAIN).entity(if (StringUtils.isBlank(e.getMessage)) "Resource not found"
        else e.getMessage).build
      case UNKNOWN_PARAMETERS | BAD_REQUEST =>
        Response.status(Response.Status.BAD_REQUEST).`type`(MediaType.TEXT_PLAIN).entity(if (StringUtils.isBlank(e.getMessage)) "Bad Request"
        else e.getMessage).build
      case _ =>
        logger.error("TwilioRestException during runtime", e)
        Response.serverError.build
    }
  }
}
