package nl.ing.api.contacting.conf.domain.model.surveysetting;

import nl.ing.api.contacting.conf.domain.validation.SurverySettingValidation;
import nl.ing.api.contacting.java.domain.OrganisationVO;

import java.util.Optional;

public record SurveyOrgMappingVO(
        Optional<Long> id,
        long surveyId,
        OrganisationVO organisationVO
) {
    public SurveyOrgMappingVO {
        SurverySettingValidation.validateId(id);
        SurverySettingValidation.validateSurveyId(surveyId);
        SurverySettingValidation.validateOrganisationVO(organisationVO);
    }
}

