package nl.ing.api.contacting.conf.domain.model.surveysetting;

import java.time.LocalDateTime;

public record SurveyCallRecordVO(
        String accountFriendlyName,
        String phoneNum,
        String surveyName,
        LocalDateTime offeredDatetime
) {}

