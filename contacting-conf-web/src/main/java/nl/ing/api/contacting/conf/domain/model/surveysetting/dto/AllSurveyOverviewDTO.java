package nl.ing.api.contacting.conf.domain.model.surveysetting.dto;

import java.util.List;

public record AllSurveyOverviewDTO(
    List<SurveyOverviewDTO> dtos
) {}

