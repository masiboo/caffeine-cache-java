package nl.ing.api.contacting.conf.resource.jaxrs.support

import com.ing.api.contacting.dto.context.SlickAuditContext
import com.ing.api.contacting.dto.audit.AuditContext
import nl.ing.api.contacting.trust.rest.context.{EmployeeContext, SessionContext}
import nl.ing.api.contacting.util.OptionConversions._

import scala.language.implicitConversions

trait AuditContextSupport {

  protected def auditContext(sessionContext: SessionContext): AuditContext =
    AuditContext(getEmployeeId(Option(sessionContext)),
      sessionContext.getSubAccount.toOption.map(_.friendlyName))

  protected def auditContext(employeeContext: EmployeeContext): AuditContext =
    AuditContext(employeeContext.getEmployeeId,
      employeeContext.getSubAccount.toOption.map(_.friendlyName))

  implicit protected def slickAuditContext(sessionContext: Option[SessionContext]): SlickAuditContext =
    SlickAuditContext(getEmployeeId(sessionContext),
      None,
      sessionContext.flatMap(_.getSubAccount.toOption.map(_.id)),
      None)

  implicit protected def slickAuditContext(employeeContext: EmployeeContext): SlickAuditContext =
    SlickAuditContext(employeeContext.getEmployeeId, None, employeeContext.getSubAccount.toOption.map(_.id), None)

  private def getEmployeeId: Option[SessionContext] => String = {
    case None => "System"
    case Some(ctx) =>
      ctx.trustContext match {
        case ctx: EmployeeContext => ctx.getEmployeeId
        case _ => "System"
      }
  }
}
