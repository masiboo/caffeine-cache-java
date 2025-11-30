package nl.ing.api.contacting.conf.domain.model.surveysetting;

import java.util.Optional;

public record SurveyPhoneNumberFormat(
        Optional<Long> id,
        long surveyId,
        String format,
        boolean direction
) {
}

