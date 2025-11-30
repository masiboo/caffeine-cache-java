package nl.ing.api.contacting.conf.domain.model.surveysetting.dto;

public record SurveyOrganisationDTO(
    String name,
    String level,
    SurveyOrganisationDTO parent
) {}

