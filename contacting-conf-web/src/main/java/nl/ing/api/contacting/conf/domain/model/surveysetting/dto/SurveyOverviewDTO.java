package nl.ing.api.contacting.conf.domain.model.surveysetting.dto;

import java.util.Optional;

public record SurveyOverviewDTO(
    Optional<Long> id,
    String name,
    String channel,
    String voiceSurveyId
) {}

