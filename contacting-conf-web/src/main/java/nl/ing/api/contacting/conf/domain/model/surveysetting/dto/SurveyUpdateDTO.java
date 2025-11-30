package nl.ing.api.contacting.conf.domain.model.surveysetting.dto;

import nl.ing.api.contacting.conf.exception.Errors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record SurveyUpdateDTO(
    SurveySettingDTO settings,
    List<SurveyPhoneNumberFormatUpdateDTO> formatsAdded,
    List<SurveyPhoneNumberFormatUpdateDTO> formatsRemoved
) {
    private static final java.util.regex.Pattern E164_PATTERN = java.util.regex.Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final java.util.regex.Pattern WILDCARD_PATTERN = java.util.regex.Pattern.compile("^\\+?[1-9]\\d{0,13}\\*$");

    public static void validatePhFormat(SurveyUpdateDTO surveyUpdateDTO) {
        //use combine streams to validate both added and removed formats
        Stream.concat(
                Optional.ofNullable(surveyUpdateDTO.formatsAdded()).orElse(List.of()).stream(),
                Optional.ofNullable(surveyUpdateDTO.formatsRemoved()).orElse(List.of()).stream()
        )
        .map(SurveyPhoneNumberFormatUpdateDTO::format)
        .filter(format -> !E164_PATTERN.matcher(format).matches() && !WILDCARD_PATTERN.matcher(format).matches())
        .findFirst()
        .ifPresent(invalid -> {
            throw Errors.badRequest("Phone number format not acceptable: " + invalid);
        });
    }

    public SurveyUpdateDTO withSettings(SurveySettingDTO newSettings) {
        return new SurveyUpdateDTO(
                newSettings,
                this.formatsAdded,
                this.formatsRemoved
        );
    }



}
