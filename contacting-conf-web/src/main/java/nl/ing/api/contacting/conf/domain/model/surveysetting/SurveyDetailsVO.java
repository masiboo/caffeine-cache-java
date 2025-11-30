package nl.ing.api.contacting.conf.domain.model.surveysetting;

import java.util.List;

public record SurveyDetailsVO(
        SurveySettingVO surveySettingVO,
        List<SurveyPhoneNumberFormatVO> phNumFormats,
        List<SurveyTaskQMappingVO> taskQMappingVO,
        List<SurveyOrgMappingVO> orgMappingVO
) {}
