package nl.ing.api.contacting.conf.business.permissions

import cats.effect.Async
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.domain.PermissionBusinessFunctionVO
import nl.ing.api.contacting.tracing.Trace
import nl.ing.api.contacting.trust.rest.context.AuthorizationContext

/**
 * @author Ayush Mittal
 */
class PermissionReaderService[F[_] : Async : Trace](permissionAlgebra: PermissionAlgebra[F]) extends LazyLogging {

  /**
   * fetch all permissions for the logged in user. All accounts will get the 'system tooling' business function with role 'contacting'
   * Maybe in the future we will need to refactor this into having defaults in cassandra and make them overloadable by the given account
   *
   * @param authorizationContext the login context which provides the user
   * @param account              the active account for the user, only need the account Friendly Name
   * @return Seq of Permissions with per business function the max organisational restriction
   */
  def fetchPermissions(authorizationContext: AuthorizationContext, accountFriendlyName: String): F[PermissionBusinessFunctionVO] = {
    permissionAlgebra.getPermissions(authorizationContext, accountFriendlyName)
  }
}
