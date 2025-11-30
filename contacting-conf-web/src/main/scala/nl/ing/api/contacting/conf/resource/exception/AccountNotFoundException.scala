package nl.ing.api.contacting.conf.resource.exception

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.{ExceptionMapper, Provider}
import nl.ing.api.contacting.conf.exception.AccountNotFoundException
import org.slf4j.LoggerFactory.getLogger

object AccountNotFoundExceptionMapper {
  private val LOGGER = getLogger(classOf[AccountNotFoundExceptionMapper])
}
@Provider
class AccountNotFoundExceptionMapper extends ExceptionMapper[AccountNotFoundException] {
  override def toResponse(exception: AccountNotFoundException): Response = {
    AccountNotFoundExceptionMapper.LOGGER.warn(exception.getMessage)
    Response.status(Response.Status.FORBIDDEN).entity(exception.getMessage).build
  }
}
