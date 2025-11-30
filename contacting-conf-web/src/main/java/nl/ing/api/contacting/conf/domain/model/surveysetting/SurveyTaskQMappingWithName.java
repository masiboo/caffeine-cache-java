package nl.ing.api.contacting.conf.domain.model.surveysetting;

import nl.ing.api.contacting.conf.domain.entity.SurveyTaskQueueMappingEntity;

/**
 * Java record representing a SurveyTaskQMapping and its associated TaskQueue friendly name.
 */
public record SurveyTaskQMappingWithName(
        SurveyTaskQueueMappingEntity mapping,
        String friendlyName
) {}

