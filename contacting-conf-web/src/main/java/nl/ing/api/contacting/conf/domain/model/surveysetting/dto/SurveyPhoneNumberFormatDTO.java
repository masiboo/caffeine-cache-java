package nl.ing.api.contacting.conf.domain.model.surveysetting.dto;

import java.util.Optional;

public record SurveyPhoneNumberFormatDTO(
    Optional<Long> id,
    String format
) {}

