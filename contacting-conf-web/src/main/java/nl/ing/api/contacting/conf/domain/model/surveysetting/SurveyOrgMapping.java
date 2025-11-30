package nl.ing.api.contacting.conf.domain.model.surveysetting;

import java.util.Optional;

public record SurveyOrgMapping(
        Optional<Long> id,
        long surveyId,
        long orgId
) {}

