package nl.ing.api.contacting.conf.business.client

import cats.effect.IO
import com.ing.api.contacting.dto.resource.permissions.PermissionsDto
import com.typesafe.scalalogging.LazyLogging
import io.opentelemetry.api.trace.Span
import nl.ing.api.contacting.conf.mapper.permissions.PermissionsMapper.toDto
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.modules.{CoreModule, ExecutionContextConfig}
import nl.ing.api.contacting.tracing.ContactingSpan
import nl.ing.api.contacting.trust.rest.context.{APISystemContext, AuthorizationContext, SessionContext}
import nl.ing.api.contacting.trust.rest.provider.PermissionProvider

import scala.concurrent.Future

/**
 * Provides permissions for contacting api resources by calling the service directly
 */
class ContactingPermissionsProvider(systemModule: CoreModule) extends PermissionProvider with LazyLogging {
  override def fetchPermissions(sessionContext: SessionContext): Future[Option[PermissionsDto]] = {
    val span: ContactingSpan[IO] =
      ContactingSpan[IO](systemModule.tracer, Option(Span.current()))
   (sessionContext.trustContext match {
      case apiSystemContext: APISystemContext => //context for system requests for API. E.g Fetching all accounts, connections..
        logger.debug(s"Returning $systemToolingPermission for ${apiSystemContext.apiName}")
        Option(Future.successful(Option(systemToolingPermission)))
      case authorizationContext: AuthorizationContext =>
        authorizationContext.subAccount.map {
          account =>
            systemModule.permissionReader
              .fetchPermissions(authorizationContext, account.friendlyName)
              .run(span)
              .unsafeToFuture()(ExecutionContextConfig.ioRunTime)
              .map(p => Option(toDto(p))) //TODO:  Option or NoPermission are not differentiated by trust
        }
      case _ => None
    }).getOrElse(Future.successful(None))
  }
}
