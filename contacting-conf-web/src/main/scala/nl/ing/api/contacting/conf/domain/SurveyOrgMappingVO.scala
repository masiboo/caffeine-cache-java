package nl.ing.api.contacting.conf.domain

import nl.ing.api.contacting.conf.domain.types.ContactingTypes.DatabaseId
import nl.ing.api.contacting.domain.slick.OrganisationVO

case class SurveyOrgMappingVO(id: Option[DatabaseId], surveyId: DatabaseId, organisationVO: OrganisationVO)