package nl.ing.api.contacting.conf.util

import jakarta.ws.rs.core.Response.Status
import jakarta.ws.rs.core.{MediaType, Response, UriBuilder}
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus

object Responses {

  def notFound(msg: String): Response =
    Response.status(Response.Status.NOT_FOUND).entity(msg).build

  def optJsonResponse[A](value: Option[A], notFound: => Response) =
    value match {
      case Some(a) => okJsonResponse(a)
      case None => notFound
    }

  def emptyOkResponse: Response =
    Response.ok.build

  def noContentResponse: Response =
    Response.noContent.build

  /**
   * Create a multi status response
   * - Response containing multiple responses eg: Bulk update responses
   * @param value response body entity containing the multiple responses
   * @tparam A response body entity type
   * @return multi status response
   */
  def multiStatusContent[A](value: A): Response =
    Response.status(HttpStatus.MULTI_STATUS.value).entity(value).build()

  def deleteResponse(rowsDeleted: Int): Response =
    rowsDeleted match {
      case 0 => notFound("resource not found")
      case _ => noContentResponse
    }

  def okJsonResponse[A](value: A): Response =
    Response.ok(value).`type`(MediaType.APPLICATION_JSON).build

  def createdResponse(classOf: Class[_], path: String): Response =
    Response.created(UriBuilder.fromResource(classOf).path(path).build()).`type`(MediaType.APPLICATION_JSON).build

  def okResponse[A](value: A): Response =
    Response.ok(value).build

  def serverError(message: Option[String] = None): Response =
    Response.status(Status.INTERNAL_SERVER_ERROR).entity(message.getOrElse(StringUtils.EMPTY)).build

  def forbidden[A](value: A): Response =
    Response.status(403).`type`(MediaType.TEXT_PLAIN).entity(value).build()

  def conflict[A](value: A): Response =
    Response.status(409).entity(value).build()

  def badRequest[A](value: A): Response =
    Response.status(400).`type`(MediaType.TEXT_PLAIN).entity(value).build()

}
