package nl.ing.api.contacting.conf.domain.model.surveysetting;

import nl.ing.api.contacting.conf.domain.entity.SurveyOrgMappingEntity;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;

public record SurveyOrgDetails(
        SurveyOrgMappingEntity mapping,
        OrganisationEntity org,
        OrganisationEntity parent,
        OrganisationEntity grandParent
) {}
