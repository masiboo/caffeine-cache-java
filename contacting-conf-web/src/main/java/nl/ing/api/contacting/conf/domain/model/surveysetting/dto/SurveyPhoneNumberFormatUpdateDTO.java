package nl.ing.api.contacting.conf.domain.model.surveysetting.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Optional;

public record SurveyPhoneNumberFormatUpdateDTO(
    @JsonDeserialize(contentAs = Long.class) Optional<Long> id,
    String format,
    int direction
) {}

