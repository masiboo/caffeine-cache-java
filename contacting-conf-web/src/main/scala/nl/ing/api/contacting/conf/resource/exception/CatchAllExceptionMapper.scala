package nl.ing.api.contacting.conf.resource.exception

import com.typesafe.scalalogging.LazyLogging
import jakarta.ws.rs.core.{MediaType, Response}
import jakarta.ws.rs.ext.{ExceptionMapper, Provider}

/**
 * Created on 16-11-2021
 *
 * @author sr56tf
 */
@Provider
class CatchAllExceptionMapper extends ExceptionMapper[Throwable] with LazyLogging {

  override def toResponse(throwable: Throwable): Response = {
    logger.error("Unhandled exception, returning 500", throwable)
    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error")
      .`type`(MediaType.TEXT_PLAIN).build
  }
}
