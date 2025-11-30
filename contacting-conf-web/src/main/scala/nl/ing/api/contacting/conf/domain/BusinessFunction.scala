package nl.ing.api.contacting.conf.domain

import nl.ing.api.contacting.trust.rest.feature.permissions.OrganisationalRestrictionLevel

/**
  * Business functions data class for Permissions based on business functions
  *
  * @param accountFriendlyName twilio sub account friendly name
  * @param businessFunction    the business function like user-admin or listen-recordings
  * @param role                the required role to access the business function
  * @param restriction         the restriction to perform the business function. Like you are to allowed to listen to recordings within your circle
  */
case class BusinessFunctionVO(accountFriendlyName: String,
                              businessFunction: String,
                              role: String,
                              restriction: OrganisationalRestrictionLevel,
                              organisationId: Int)



sealed trait PermissionBusinessFunctionVO {

  def businessFunctions: Seq[BusinessFunctionVO]

  def organisation: Option[PermissionOrganisationVO]
}

case class EmployeeBusinessFunctionVO(
    organisation: Option[PermissionOrganisationVO],
    businessFunctions: Seq[BusinessFunctionVO])
    extends PermissionBusinessFunctionVO

case class NonEmployeeBusinessFunctionVO(
    businessFunctions: Seq[BusinessFunctionVO])
    extends PermissionBusinessFunctionVO {
  override def organisation: Option[PermissionOrganisationVO] = None
}

/**
  * Domain class to flatten the organisation structure in cassandra / kafka. The organisationalRestriction contains teams the member can access.
  *
  * @param cltId The CLT ID the worker belongs to
  * @param cltName The CLT the worker belongs to
  * @param circleId The circle ID the worker belongs to
  * @param circleName The circle the worker belongs to
  * @param superCircleId The SuperCircle ID the worker belongs to
  * @param superCircleName The SuperCircle the worker belongs to
  * @param organisationalRestriction a set with all teams the member can access
  */
case class PermissionOrganisationVO(
    cltId: Int,
    cltName: String,
    circleId: Int,
    circleName: String,
    superCircleId: Int,
    superCircleName: String,
    organisationalRestriction: Set[OrganisationalRestriction])

case class AccessLevelByRoleVO(role: String,
                               level: OrganisationalRestrictionLevel)
