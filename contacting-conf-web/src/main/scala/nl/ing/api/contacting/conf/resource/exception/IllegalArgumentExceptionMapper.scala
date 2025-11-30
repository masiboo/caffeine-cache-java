package nl.ing.api.contacting.conf.resource.exception

import jakarta.ws.rs.core.{MediaType, Response}
import jakarta.ws.rs.ext.{ExceptionMapper, Provider}

/**
 * Created on 16/08/2020 at 22:56
 *
 * @author bo55nk
 */
@Provider
class IllegalArgumentExceptionMapper extends ExceptionMapper[IllegalArgumentException] {
  override def toResponse(exception: IllegalArgumentException): Response = {
    Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage)
      .`type`(MediaType.TEXT_PLAIN).build
  }
}
