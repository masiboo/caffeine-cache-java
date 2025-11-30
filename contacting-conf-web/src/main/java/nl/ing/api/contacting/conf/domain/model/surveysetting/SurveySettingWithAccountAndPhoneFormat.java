package nl.ing.api.contacting.conf.domain.model.surveysetting;

import nl.ing.api.contacting.conf.domain.entity.SurveySettingsEntity;
import nl.ing.api.contacting.conf.repository.model.SurveyPhoneNumberFormat;
import nl.ing.api.contacting.domain.slick.AccountVO;

public record SurveySettingWithAccountAndPhoneFormat(
        SurveySettingsEntity surveySetting,
        AccountVO account,
        SurveyPhoneNumberFormat phoneNumberFormat
) {}
