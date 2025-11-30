package nl.ing.api.contacting.conf.business.permissions

import cats.Monad
import cats.effect.Async
import cats.implicits._
import com.ing.api.contacting.dto.resource.permissions.BusinessFunctionDto
import com.ing.api.contacting.dto.resource.permissions.PermissionsDto
import com.typesafe.scalalogging.LazyLogging
import io.opentelemetry.api.trace.Span
import nl.ing.api.contacting.conf.domain.{BusinessFunctionVO, EmployeeAccountsVO, EmployeeBusinessFunctionVO, NonEmployeeBusinessFunctionVO, PermissionBusinessFunctionVO, PermissionOrganisationVO}
import nl.ing.api.contacting.conf.domain.enums.{CONTACTING, CUSTOMER_AUTHENTICATED, CUSTOMER_UNAUTHENTICATED, ContactingBusinessFunctionsScala, FOREIGN_API}
import nl.ing.api.contacting.conf.mapper.permissions.PermissionsMapper.ORG_ID_FOR_ACCOUNT
import nl.ing.api.contacting.tracing.Trace
import nl.ing.api.contacting.trust.rest.context._
import nl.ing.api.contacting.trust.rest.feature.permissions.ACCOUNT

import scala.language.higherKinds

/**
 * @author Ayush Mittal
 */
object PermissionAlgebra {

  private val systemTooling =
    BusinessFunctionVO("",
                       ContactingBusinessFunctionsScala.SYSTEM_TOOLING,
                       "CONTACTING",
                       ACCOUNT,
                       ORG_ID_FOR_ACCOUNT)

  val systemToolingPermission = PermissionsDto(None, Set.empty, Seq(BusinessFunctionDto("system tooling", "CONTACTING", ACCOUNT.level, None)))

  private def filterMaxRestrictions(businessFunctions: Seq[BusinessFunctionVO],
                                    roles: Set[String]): Seq[BusinessFunctionVO] = {
    // first filter all the business functions which are allowed to access, then return the max organisational restriction for that functions
    filterAllowed(businessFunctions, roles)
      .groupBy(a => (a.businessFunction, a.organisationId))
      .mapValues(_.maxBy(_.restriction.level))
      .values
      .toSeq
  }

  private def filterAllowed(businessFunctions: Seq[BusinessFunctionVO], roles: Set[String]): Seq[BusinessFunctionVO] = {
    businessFunctions.filter(bf => roles.contains(bf.role))
  }

  private def createNonEmployeePermissionVO(businessFunctions: Seq[BusinessFunctionVO],
                                            roles: Set[String]): PermissionBusinessFunctionVO = {
    NonEmployeeBusinessFunctionVO(filterMaxRestrictions(businessFunctions, roles))
  }

  private def createEmployeePermissionVO(organisation: Option[PermissionOrganisationVO],
                                         businessFunctions: Seq[BusinessFunctionVO],
                                         roles: Set[String]): PermissionBusinessFunctionVO = {
    EmployeeBusinessFunctionVO(organisation, filterMaxRestrictions(businessFunctions, roles))
  }
}

class PermissionAlgebra[F[_]: Async: Trace](dataSource: BusinessFunctionDataSource[F]) extends LazyLogging {

  import PermissionAlgebra._

  def getPermissionsForEmployee(employeeId: String, accountFriendlyName: String): F[PermissionBusinessFunctionVO] = {
    for {
      allBusinessFunctions <- getAllBusinessFunctions(accountFriendlyName)
      permissions <- getPermissionsForEmployee(employeeId, accountFriendlyName, allBusinessFunctions)
    } yield permissions
  }

  def getPermissions(authContext: AuthorizationContext, accountFriendlyName: String): F[PermissionBusinessFunctionVO] = {
    for {
      allBusinessFunctions <- getAllBusinessFunctions(accountFriendlyName)
      permissions <- getPermissions(authContext, accountFriendlyName, allBusinessFunctions)
    } yield permissions
  }

  def getPermissions(authContext: AuthorizationContext,
                     accountFriendlyName: String,
                     businessFunctions: Seq[BusinessFunctionVO]): F[PermissionBusinessFunctionVO] = {
    authContext match {
      case ectx: EmployeeContext =>
        getPermissionsForEmployee(ectx.employeeId, accountFriendlyName, businessFunctions)
      case _: CustomerContext =>
        createNonEmployeePermissionVO(businessFunctions, Set(CUSTOMER_AUTHENTICATED.role)).pure(Monad[F])
      case _: ContactingApiContext =>
        createNonEmployeePermissionVO(businessFunctions, Set(CONTACTING.role)).pure(Monad[F])
      case _: ForeignApiContext =>
        createNonEmployeePermissionVO(businessFunctions, Set(FOREIGN_API.role)).pure(Monad[F])
      case _ =>
        createNonEmployeePermissionVO(businessFunctions, Set(CUSTOMER_UNAUTHENTICATED.role)).pure(Monad[F])
    }
  }

  private def getPermissionsForEmployee(employeeId: String,
                                        accountFriendlyName: String,
                                        businessFunctions: Seq[BusinessFunctionVO]): F[PermissionBusinessFunctionVO] =
    Trace[F].span("Permissions for employee") {
      for {
        employeeOpt <- dataSource.findByEmployeeId(employeeId, accountFriendlyName)
        result <- getPermissionsForEmployeeContext(employeeOpt, businessFunctions)
      } yield result
    }

  def getPermissionsForEmployeeContext(employee: Option[EmployeeAccountsVO],
                                       businessFunctions: Seq[BusinessFunctionVO]): F[PermissionBusinessFunctionVO] = {
    (employee match {
      case Some(emp) if emp.organisationalRestrictions.isDefined =>
        emp.organisationalRestrictions.map(_.find(_.preferred)).get match {
          case Some(preferredTeam) =>
            createEmployeePermissionVO(
              Some(
                PermissionOrganisationVO(
                  preferredTeam.cltId,
                  preferredTeam.cltName,
                  preferredTeam.circleId,
                  preferredTeam.circleName,
                  preferredTeam.superCircleId,
                  preferredTeam.superCircleName,
                  emp.organisationalRestrictions.getOrElse(Set())
                )),
              businessFunctions,
              emp.rolesAsSet()
            )
          case None =>
            createEmployeePermissionVO(None, businessFunctions, emp.rolesAsSet())
        }
      case Some(emp) =>
        createEmployeePermissionVO(None, businessFunctions, emp.rolesAsSet())
      case None =>
        createEmployeePermissionVO(None, businessFunctions, Set())
    }).pure(Monad[F])
  }

  private def getAllBusinessFunctions(accountFriendlyname: String): F[Seq[BusinessFunctionVO]] = {
    dataSource.findByAccount(accountFriendlyname).map {
      businessFunctions =>
        systemTooling.copy(accountFriendlyName = accountFriendlyname) +: businessFunctions
    }
  }

}
