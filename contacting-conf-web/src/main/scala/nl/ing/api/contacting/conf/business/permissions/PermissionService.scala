package nl.ing.api.contacting.conf.business.permissions

import com.ing.api.contacting.dto.audit.AuditContext
import com.ing.apisdk.toolkit.logging.audit.api.{ActionStatus, AuditEvent, Severity}
import com.ing.apisdk.toolkit.logging.audit.slf4j.Slf4jAuditLogger
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.business.ConfigService
import nl.ing.api.contacting.conf.business.permissions.PermissionService.{BUSINESS_FUNCTIONS, BUSINESS_FUNCTIONS_AT_TEAM_LEVEL, READONLY_BUSINESS_FUNCTIONS, ROLES}
import nl.ing.api.contacting.conf.domain.{BusinessFunctionVO, ContactingConfigVO}
import nl.ing.api.contacting.conf.mapper.permissions.PermissionsMapper
import org.slf4j.MarkerFactory
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.repository.cassandra.permissions.BusinessFunctionsRepository

import java.time.Instant
import scala.concurrent.Future

/**
  * Permission service to determine whether the user has the required permissions to perform a business function
  */
object PermissionService {

  /**
    * These business functions are readonly, so should not be altered by the user!
    */
  val READONLY_BUSINESS_FUNCTIONS = "BUSINESS_FUNCTIONS_HIDDEN"
  val BUSINESS_FUNCTIONS_AT_TEAM_LEVEL = "BUSINESS_FUNCTIONS_AT_TEAM_LEVEL"
  val BUSINESS_FUNCTIONS = "BUSINESS_FUNCTIONS"
  val ROLES = "ROLES"
}

/**
  * Business service for managing and validating user permissions
  */
class PermissionService(configService: ConfigService,businessFunctionsRepository: BusinessFunctionsRepository[Future], auditLogger: Slf4jAuditLogger) extends LazyLogging {

  /**
    * We validate the permissions:
    * Is the user not introducing new business roles, and just changing roles/restrictions of the permission?
    * Does the user role (SELF/SUPER_CIRCLE) exist?
    * Then we diff the changes and appropriately fire off a upsert and delete queries to cassandra
    * upserts and deletes are fired off asynchronously
    *
    * @param businessFunctions a seq of business functions which will "replace" the old ones
    * @param account           the active account for the user
    * @return a future which has to be awaited. Nothing interesting inside the future
    */
  def syncBusinessFunctions(businessFunctions: Seq[BusinessFunctionVO], accountFriendlyName: String)(implicit auditContext: AuditContext): Future[Any] = {
    for {
      config <- configService.fetchConfigs()
      if validate(config, businessFunctions)
      editableBusinessFunctions <- getEditableBusinessFunctions(accountFriendlyName)
      _ <- removeRoles(accountFriendlyName, editableBusinessFunctions, businessFunctions)
      _ <- addRoles(accountFriendlyName, editableBusinessFunctions, businessFunctions)
    } yield ()
  }

  /**
    * get all the editable business functions, so we don't override system tooling
    *
    * @param account the active account for the user
    * @return a seq of editable business functions, so system tooling and permission administration will be excluded
    */
  def getEditableBusinessFunctions(accountFriendlyName: String): Future[Seq[BusinessFunctionVO]] = {
    (for {
      businessFunctions <- businessFunctionsRepository.findByAccount(accountFriendlyName)
      readonlyBusinessFunctions <- configService.findByKey(READONLY_BUSINESS_FUNCTIONS)
      editableBusinessFunctions <- Future.successful(
        businessFunctions.filterNot(bf => readonlyBusinessFunctions contains bf.businessFunction))
    } yield editableBusinessFunctions).map(_.sortBy(_.businessFunction))
  }

  /**
    * remove the roles for the given business functions.
    *
    * @param currentPermissions   the current state of the database
    * @param newPermissionsRoleVO the required state give by the user
    * @return
    */
  private def removeRoles(accountFriendlyName: String, currentPermissions: Seq[BusinessFunctionVO], newPermissionsRoleVO: Seq[BusinessFunctionVO])(
    implicit auditContext: AuditContext): Future[Any] = {
    val toRemoveRoles = currentPermissions.diff(newPermissionsRoleVO)
    businessFunctionsRepository.deletePermissions(accountFriendlyName: String, toRemoveRoles).value
      .andThen {
        case _ => auditLogger.log(new AuditEvent(Severity.MEDIUM, Instant.now(), auditContext.modifiedBy, "BUSINESS_FUNCTIONS_MAPPINGS_REMOVED", toRemoveRoles.map(role => role.role).mkString(","), ActionStatus.SUCCESS))
      }
  }

  private def addRoles(accountFriendlyName: String, currentPermissions: Seq[BusinessFunctionVO], newPermissionsRoleVO: Seq[BusinessFunctionVO])(
    implicit auditContext: AuditContext): Future[Any] = {
    val toAddRoles = newPermissionsRoleVO.diff(currentPermissions)
    businessFunctionsRepository.upsertPermissions(accountFriendlyName, toAddRoles).value
      .andThen {
        case _ => auditLogger.log(MarkerFactory.getMarker("AUDIT"), new AuditEvent(Severity.MEDIUM, Instant.now(), auditContext.modifiedBy, "BUSINESS_FUNCTIONS_MAPPINGS_ADDED", toAddRoles.map(role => role.role).mkString(","), ActionStatus.SUCCESS))
      }
  }

  /*
   * These method makes sure only fields are attempted to change which are allowed to be changed
   */
  private def validate(config: Seq[ContactingConfigVO], newPermissionsRoleVO: Seq[BusinessFunctionVO]): Boolean = {
    val validBusinessFunctions =
      config.filter(_.key == BUSINESS_FUNCTIONS).flatMap(_.valuesAsSet())
    val validTeamLevelBusinessFunctions =
      config.filter(_.key == BUSINESS_FUNCTIONS_AT_TEAM_LEVEL).flatMap(_.valuesAsSet())
    val validRoles = config.filter(_.key == ROLES).flatMap(_.valuesAsSet())
    require(newPermissionsRoleVO.forall(x => validBusinessFunctions.contains(x.businessFunction)),
      s"updating permissions with an invalid business function")
    require(newPermissionsRoleVO.forall(x => validRoles.contains(x.role)), "updating permissions with an invalid role: " + newPermissionsRoleVO.map(_.role).filterNot(validRoles.contains).mkString(","))
    require(
      newPermissionsRoleVO.forall(_.organisationId == PermissionsMapper.ORG_ID_FOR_ACCOUNT) ||
        newPermissionsRoleVO
          .filterNot(_.organisationId == PermissionsMapper.ORG_ID_FOR_ACCOUNT)
          .exists(bf => validTeamLevelBusinessFunctions.contains(bf.businessFunction)),
      s"permission at team level are only allowed for $validTeamLevelBusinessFunctions"
    )
    true
  }
}
