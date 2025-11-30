package nl.ing.api.contacting.conf.helper;

import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyDetailsVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveySettingVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.*;

import java.util.List;
import java.util.Optional;

public class SurveySettingTestData {
    private static final Long VALID_SURVEY_ID = 1L;
    private static final Long ACCOUNT_ID = 123L;
    private static final String SURVEY_NAME = "Test Survey";
    private static final String CHANNEL_CALL = "call";

    public static SurveySettingDTO surveySettingDTO() {
        return new SurveySettingDTO(
                Optional.of(VALID_SURVEY_ID),
                SURVEY_NAME,
                CHANNEL_CALL,
                "outbound",
                "voice123",
                Optional.of("test-callflow"),
                Optional.of(5),
                Optional.of(30L),
                Optional.of(75.5f),
                Optional.of(120L),
                true
        );
    }

    public static SurveySettingDTO surveySettingDTOWithoutCallflow() {
        return new SurveySettingDTO(
                Optional.empty(),
                SURVEY_NAME,
                CHANNEL_CALL,
                "inbound",
                "voice456",
                Optional.empty(),
                Optional.of(10),
                Optional.of(60L),
                Optional.of(50.0f),
                Optional.of(180L),
                false
        );
    }

    public static SurveySettingDTO surveySettingDTOWithInvalidName() {
        return new SurveySettingDTO(
                Optional.empty(),
                null,
                CHANNEL_CALL,
                "outbound",
                "voice789",
                Optional.empty(),
                Optional.of(15),
                Optional.of(45L),
                Optional.of(25.0f),
                Optional.of(90L),
                true
        );
    }

    public static SurveyUpdateDTO surveyUpdateDTO() {
        return new SurveyUpdateDTO(
                surveySettingDTO(),
                List.of(phoneFormatUpdateDTO()),
                List.of(phoneFormatUpdateDTOToRemove())
        );
    }

    public static SurveyUpdateDTO surveyUpdateDTOWithInvalidPhone() {
        return new SurveyUpdateDTO(
                surveySettingDTO(),
                List.of(invalidPhoneFormatUpdateDTO()),
                List.of()
        );
    }

    public static SurveyPhoneNumberFormatUpdateDTO phoneFormatUpdateDTO() {
        return new SurveyPhoneNumberFormatUpdateDTO(
                Optional.empty(),
                "+31612345678",
                0
       );
    }

    public static SurveyPhoneNumberFormatUpdateDTO phoneFormatUpdateDTOToRemove() {
        return new SurveyPhoneNumberFormatUpdateDTO(
                Optional.of(2L),
                "+31687654321",
               1
        );
    }

    public static SurveyPhoneNumberFormatUpdateDTO invalidPhoneFormatUpdateDTO() {
        return new SurveyPhoneNumberFormatUpdateDTO(
                Optional.empty(),
               "invalid-phone-format",
                0
        );
    }
    public static SurveyAssociationUpdateDTO associationUpdateDTO() {
        return new SurveyAssociationUpdateDTO(
                List.of(10L, 20L),
                List.of(30L, 40L)
        );
    }

    public static SurveyAssociationUpdateDTO emptyAssociationUpdateDTO() {
        return new SurveyAssociationUpdateDTO(
                List.of(),
                List.of()
        );
    }

    public static SurveySettingVO surveySettingVO() {
        return new SurveySettingVO(
                Optional.of(VALID_SURVEY_ID),
                ACCOUNT_ID,
                SURVEY_NAME,
                CHANNEL_CALL,
                "outbound",
                "voice123",
                Optional.of("test-callflow"),
                Optional.of(5),
                Optional.of(30L),
                Optional.of(75.5f),
                Optional.of(120L),
                true
        );
    }

    public static SurveyDetailsVO surveyDetailsVO() {
       return new SurveyDetailsVO(
                surveySettingVO(),
                List.of(),
                List.of(),
               List.of()
        );
   }

    public static List<SurveyOverviewDTO> mockSurveyOverviewList() {
        return List.of(
               new SurveyOverviewDTO(Optional.of(1L), "Survey 1", "call", "voice1"),
                new SurveyOverviewDTO(Optional.of(2L), "Survey 2", "email", "voice2")
        );
     }
    }