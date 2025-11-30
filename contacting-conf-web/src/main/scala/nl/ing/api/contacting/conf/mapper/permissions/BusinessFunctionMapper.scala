package nl.ing.api.contacting.conf.mapper.permissions

import com.ing.api.contacting.dto.resource.permissions.BusinessFunctionDto
import nl.ing.api.contacting.conf.domain.BusinessFunctionVO
import nl.ing.api.contacting.conf.resource.dto.{BusinessFunctionAccess, BusinessFunctionsDto}
import nl.ing.api.contacting.trust.rest.feature.permissions.OrganisationalRestrictionLevel

object BusinessFunctionMapper {

  def toDto(businessFunctions: Seq[BusinessFunctionVO]): Seq[BusinessFunctionsDto] = {
    businessFunctions.map(item => toDto(item))
      .groupBy(a => (a.businessFunction, a.organisationId))
      .map(tuple => BusinessFunctionsDto(tuple._1._1, toAccessLevelByRoleDto(tuple._2, tuple._1._2)))
      .toSeq
  }

  private def toAccessLevelByRoleDto(permissions: Seq[BusinessFunctionDto], organisationId: Option[Int]): Seq[BusinessFunctionAccess] = {
    permissions.map(permission => BusinessFunctionAccess(permission.role, permission.accessRestriction, organisationId))
  }

  private def toDto(businessFunction: BusinessFunctionVO): BusinessFunctionDto = {
    BusinessFunctionDto(businessFunction.businessFunction, businessFunction.role, businessFunction.restriction.level,
      if (businessFunction.organisationId == PermissionsMapper.ORG_ID_FOR_ACCOUNT) None else Option(businessFunction.organisationId))
  }

  def toVO(accountFriendlyName: String, businessFunction: BusinessFunctionsDto): Seq[BusinessFunctionVO] = {
    businessFunction.allowedAccess
      .map {
        allowedAccess =>
          BusinessFunctionVO(accountFriendlyName,
            businessFunction.name,
            allowedAccess.role,
            OrganisationalRestrictionLevel.fromLevel(allowedAccess.level),
            allowedAccess.organisationId.getOrElse(PermissionsMapper.ORG_ID_FOR_ACCOUNT))
      }
  }
}
