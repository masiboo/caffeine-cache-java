package nl.ing.api.contacting.conf.business.context

import com.ing.api.contacting.dto.context.{ContactingContext, SlickAuditContext}
import nl.ing.api.contacting.conf.resource.jaxrs.support.AuditContextSupport
import com.ing.api.contacting.dto.resource.account.AccountDto
import nl.ing.api.contacting.trust.rest.context.SessionContext

/**
  * @author Ayush Mittal
  */
trait ContactingContextProvider {

  def contactingContext: ContactingContext

}

private case class DefaultContactingContextProvider(
    accountDto: AccountDto,
    sessionContext: Option[SessionContext])
    extends ContactingContextProvider
    with AuditContextSupport {

  override def contactingContext: ContactingContext =
    ContactingContext(accountDto.id, slickAuditContext(sessionContext))
}

object ContactingContextProvider {

  /**
    * creates a contacting context using account dto and session context
    * @param accountDto
    * @param sessionContext
    * @return
    */
  def getContactingContext(
      accountDto: AccountDto,
      sessionContext: Option[SessionContext]): ContactingContext = {
    DefaultContactingContextProvider(accountDto, sessionContext).contactingContext
  }

  /**
    * creates a contacting context using account id; the audit context is system based
    * @param accountId
    * @return
    */
  def getSystemContactingContext(accountId: Long): ContactingContext = {
    ContactingContext(accountId,
                      SlickAuditContext("System", None, Some(accountId), None))
  }
}
