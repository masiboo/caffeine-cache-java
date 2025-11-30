package nl.ing.api.contacting.conf.resource.jaxrs.support

import com.ing.api.contacting.dto.resource.account.AccountDto
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import nl.ing.api.contacting.conf.exception.AccountNotFoundException
import nl.ing.api.contacting.trust.rest.context._
import nl.ing.api.contacting.trust.rest.request.RequestReaders.containerRequestReader
import nl.ing.api.contacting.util.OptionConversions._



/**
  * @author Ayush Mittal
  * Provides accountDto from session context
  */
trait AccountSupport {
  @Context
  var containerRequest: ContainerRequestContext = _

  protected def getSessionContext: Option[SessionContext] =
    SessionContextAttributesHelper.getSessionContext(containerRequest).toOption

  /**
    * set the current account into the implicit scope, so it will be available when calling services
    *
    * @return The account from the incoming request
    */
  implicit def account: AccountDto = {
    val context = getSessionContext
    context
      .flatMap(_.getSubAccount.toOption)
      .getOrElse(throw AccountNotFoundException(s"Request has no subAccount hence Forbidden for context: ${context.map(_.describe)}"))
  }

}
