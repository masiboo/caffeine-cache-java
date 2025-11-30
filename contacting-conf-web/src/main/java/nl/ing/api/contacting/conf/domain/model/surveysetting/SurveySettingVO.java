package nl.ing.api.contacting.conf.domain.model.surveysetting;

import java.util.Optional;

public record SurveySettingVO(
        Optional<Long> id,
        Long accountId,
        String name,
        String channel,
        String channelDirection,
        String voiceSurveyId,
        Optional<String> callflowName,
        Optional<Integer> minFrequency,
        Optional<Long> delay,
        Optional<Float> surveyOfferRatio,
        Optional<Long> minContactLength,
        boolean surveyForTransfers
) {}

