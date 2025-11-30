package nl.ing.api.contacting.conf.domain.model.surveysetting.dto;

import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyCallRecordVO;

import java.util.List;

public record OfferedSurveyCallsDTO(
    List<SurveyCallRecordVO> records
) {}
