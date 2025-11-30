package nl.ing.api.contacting.conf.domain.model.surveysetting.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public record SurveyAssociationUpdateDTO(
    @JsonDeserialize(contentAs = Long.class) List<Long> idsAdded,
    @JsonDeserialize(contentAs = Long.class) List<Long> idsRemoved
) {}

