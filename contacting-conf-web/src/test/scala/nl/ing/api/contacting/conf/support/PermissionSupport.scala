package nl.ing.api.contacting.conf.support

import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.domain.{BusinessFunctionVO, EmployeeAccountsVO, OrganisationalRestriction}
import nl.ing.api.contacting.conf.domain.enums._
import nl.ing.api.contacting.conf.mapper.permissions.PermissionsMapper.ORG_ID_FOR_ACCOUNT
import nl.ing.api.contacting.trust.rest.feature.permissions._

/**
 * @author Ayush Mittal
 */
trait PermissionSupport extends BaseSpec with AccountSupport {

  val permissions = List(
    BusinessFunctionVO(account.friendlyName, "listen-recordings", AGENT.role, TEAM, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "listen-recordings", SUPERVISOR.role, CIRCLE, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "listen-recordings", ADMIN.role, SUPER_CIRCLE, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "chat", AGENT.role, SELF, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "chat", CUSTOMER_AUTHENTICATED.role, SELF, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "chat", CUSTOMER_UNAUTHENTICATED.role, SELF, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "user-management", SUPERVISOR.role, TEAM, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "user-management", ADMIN.role, CIRCLE, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "user-management", ACCOUNT_ADMIN.role, ACCOUNT, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "inbound-calls", AGENT.role, SELF, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "statistics", CONTACTING.role, ACCOUNT, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account.friendlyName, "statistics", AGENT.role, SELF, ORG_ID_FOR_ACCOUNT)
  )

  val userWithAdmin = EmployeeAccountsVO(
    "ADMIN1",
    "NL",
    preferredAccount = true,
    Set(ADMIN.role),
    Some("bu-1"),
    Some("dep-1"),
    Some("team-1"),
    Some(Seq(OrganisationalRestriction(1, "team-1", 2, "dep-1", 3, "bu-1", true))),
    Map.empty[String, Int],
    None
  )

  val userWithSuperVisorAndAdmin = EmployeeAccountsVO(
    "ACCOUNT_ADMIN1",
    "NL",
    preferredAccount = true,
    Set(SUPERVISOR.role, ACCOUNT_ADMIN.role),
    Some("bu-1"),
    Some("dep-1"),
    Some("team-1"),
    Some(Seq(OrganisationalRestriction(1, "team-1", 2, "dep-1", 3, "bu-1", true))),
    Map.empty[String, Int],
    None
  )
}
