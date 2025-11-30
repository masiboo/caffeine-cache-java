package nl.ing.api.contacting.conf.domain.model.surveysetting;

import java.util.List;

public record SurveyUpdateVO(
        SurveySettingVO settings,
        List<SurveyPhoneNumberFormatVO> formatsAdded,
        List<SurveyPhoneNumberFormatVO> formatsRemoved
) {}
