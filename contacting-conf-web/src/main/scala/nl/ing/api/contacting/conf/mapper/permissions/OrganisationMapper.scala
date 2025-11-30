package nl.ing.api.contacting.conf.mapper.permissions

import com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto
import nl.ing.api.contacting.conf.domain.{OrganisationalRestriction, PermissionOrganisationVO}

object OrganisationMapper {

  def toDto(org: PermissionOrganisationVO):FlatOrganisationUnitDto =
    FlatOrganisationUnitDto(org.cltId, org.cltName, org.circleId, org.circleName, org.superCircleId, org.superCircleName)

  def toDto(org: OrganisationalRestriction):FlatOrganisationUnitDto =
    FlatOrganisationUnitDto(org.cltId, org.cltName, org.circleId, org.circleName, org.superCircleId, org.superCircleName)
}
