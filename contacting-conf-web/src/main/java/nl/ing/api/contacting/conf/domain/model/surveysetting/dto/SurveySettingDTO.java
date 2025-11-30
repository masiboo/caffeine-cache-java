package nl.ing.api.contacting.conf.domain.model.surveysetting.dto;

import com.esotericsoftware.kryo.NotNull;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import nl.ing.api.contacting.conf.exception.Errors;

import java.util.Optional;

public record SurveySettingDTO(
    @JsonDeserialize(contentAs = Long.class)
    @NotNull @Min(1)
    Optional<Long> id,

    @NotBlank
    String name,

    String channel,
    String channelDirection,
    String voiceSurveyId,
    Optional<String> callflowName,

    @Min(0)
    Optional<Integer> minFrequency,

    @JsonDeserialize(contentAs = Long.class)
    Optional<Long> delay,

    @DecimalMin("0.0") @DecimalMax("100.0")
    @JsonDeserialize(contentAs = Float.class)
    Optional<Float> surveyOfferRatio,

    @Min(0)
    @JsonDeserialize(contentAs = Long.class)
    Optional<Long> minContactLength,

    boolean surveyForTransfers
) {
    public static void validateCallflowForChannel(SurveySettingDTO dto) {
        if ("call".equalsIgnoreCase(dto.channel) && dto.callflowName().isEmpty()) {
            throw Errors.badRequest("Callflow is mandatory when channel is call");
        }
    }

    public static void validateName(SurveySettingDTO dto) {
        if (dto.name == null || dto.name.length() > 255) {
            throw Errors.badRequest("survey setting name length must be between 0 and 255");
        }
    }

    public SurveySettingDTO withId(Long newId) {
        return new SurveySettingDTO(
                Optional.of(newId),
                this.name,
                this.channel,
                this.channelDirection,
                this.voiceSurveyId,
                this.callflowName,
                this.minFrequency,
                this.delay,
                this.surveyOfferRatio,
                this.minContactLength,
                this.surveyForTransfers
        );
    }




}
