package nl.ing.api.contacting.conf.mapper.permissions

import com.ing.api.contacting.dto.resource.permissions.{BusinessFunctionDto, PermissionsDto}
import nl.ing.api.contacting.conf.domain.{BusinessFunctionVO, PermissionBusinessFunctionVO}

/**
  * Created by Remco de Bruin on 31-7-17.
  */
object PermissionsMapper {

  val ORG_ID_FOR_ACCOUNT: Int = -1

  def toDto(permissionsVO: PermissionBusinessFunctionVO): PermissionsDto = {
    val organisation = permissionsVO.organisation.map(OrganisationMapper.toDto)
    val organisations = permissionsVO.organisation.map(_.organisationalRestriction.map(OrganisationMapper.toDto))
    val businessFunctions: Seq[BusinessFunctionDto] = permissionsVO.businessFunctions.map(bf => BusinessFunctionDto(bf.businessFunction, bf.role, bf.restriction.level, getOrganisationIdIfPresent(bf)))
    PermissionsDto(organisation, organisations.getOrElse(Set()), businessFunctions)
  }

  private def getOrganisationIdIfPresent(bf: BusinessFunctionVO) = if (bf.organisationId > ORG_ID_FOR_ACCOUNT) Option(bf.organisationId) else None

}
