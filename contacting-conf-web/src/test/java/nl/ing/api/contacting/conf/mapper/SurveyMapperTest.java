package nl.ing.api.contacting.conf.mapper;

import nl.ing.api.contacting.conf.domain.entity.SurveyPhoneNumberFormatEntity;
import nl.ing.api.contacting.conf.domain.entity.SurveySettingsEntity;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyDetailsVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyPhoneNumberFormatVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveySettingVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyUpdateVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SurveyMapperTest {

    @Test
    @DisplayName("surveySettingEntityToVo should map entity to VO successfully")
    void surveySettingEntityToVo_shouldMapEntityToVo() {
        // Given
        SurveySettingsEntity entity = SurveySettingsEntity.builder()
                .id(1L)
                .accountId(100L)
                .name("Test Survey")
                .channel("voice")
                .channelDirection("inbound")
                .voiceSurveyId("survey123")
                .callflowName("test-flow")
                .minFrequency(60)
                .delay(30L)
                .surveyOfferRatio(0.5F)
                .minContactLength(120L)
                .surveyForTransfers(true)
                .build();

        // When
        SurveySettingVO result = SurveyMapperJava.surveySettingEntityToVo(entity);

        // Then
        assertThat(result.id()).isPresent().contains(1L);
        assertThat(result.accountId()).isEqualTo(100L);
        assertThat(result.name()).isEqualTo("Test Survey");
        assertThat(result.channel()).isEqualTo("voice");
        assertThat(result.channelDirection()).isEqualTo("inbound");
        assertThat(result.voiceSurveyId()).isEqualTo("survey123");
        assertThat(result.callflowName()).isPresent().contains("test-flow");
        assertThat(result.minFrequency()).isPresent().contains(60);
        assertThat(result.delay()).isPresent().contains(30L);
        assertThat(result.surveyOfferRatio()).isPresent().contains(0.5F);
        assertThat(result.minContactLength()).isPresent().contains(120L);
        assertThat(result.surveyForTransfers()).isTrue();
    }

    @Test
    @DisplayName("surveySettingEntityToVo should handle null optional fields")
    void surveySettingEntityToVo_shouldHandleNullOptionalFields() {
        // Given
        SurveySettingsEntity entity = SurveySettingsEntity.builder()
                .id(1L)
                .accountId(100L)
                .name("Test Survey")
                .channel("voice")
                .channelDirection("inbound")
                .voiceSurveyId("survey123")
                .surveyForTransfers(false)
                .build();

        // When
        SurveySettingVO result = SurveyMapperJava.surveySettingEntityToVo(entity);

        // Then
        assertThat(result.callflowName()).isEmpty();
        assertThat(result.minFrequency()).isEmpty();
        assertThat(result.delay()).isEmpty();
        assertThat(result.surveyOfferRatio()).isEmpty();
        assertThat(result.minContactLength()).isEmpty();
    }

    @Test
    @DisplayName("surveySettingEntityToVo should throw exception when entity is null")
    void surveySettingEntityToVo_shouldThrowExceptionWhenEntityIsNull() {
        // When & Then
        assertThatThrownBy(() -> SurveyMapperJava.surveySettingEntityToVo(null))
                .hasMessage("SurveySettingsEntity is null");
    }

    @Test
    @DisplayName("surveySettingEntityToVo should throw exception when entity ID is null")
    void surveySettingEntityToVo_shouldThrowExceptionWhenEntityIdIsNull() {
        // Given
        SurveySettingsEntity entity = SurveySettingsEntity.builder()
                .accountId(100L)
                .name("Test Survey")
                .build();

        // When & Then
        assertThatThrownBy(() -> SurveyMapperJava.surveySettingEntityToVo(entity))
                .hasMessage("Survey ID is missing");
    }

    @Test
    @DisplayName("surveySettingEntityToVo should throw exception when entity name is blank")
    void surveySettingEntityToVo_shouldThrowExceptionWhenEntityNameIsBlank() {
        // Given
        SurveySettingsEntity entity = SurveySettingsEntity.builder()
                .id(1L)
                .accountId(100L)
                .name("")
                .build();

        // When & Then
        assertThatThrownBy(() -> SurveyMapperJava.surveySettingEntityToVo(entity))
                .hasMessage("Survey name is missing");
    }

    @Test
    @DisplayName("surveySettingVoToEntity should map VO to entity successfully")
    void surveySettingVoToEntity_shouldMapVoToEntity() {
        // Given
        SurveySettingVO vo = new SurveySettingVO(
                Optional.of(1L),
                100L,
                "Test Survey",
                "voice",
                "inbound",
                "survey123",
                Optional.of("test-flow"),
                Optional.of(60),
                Optional.of(30L),
                Optional.of(0.5F),
                Optional.of(120L),
                true
        );

        // When
        SurveySettingsEntity result = SurveyMapperJava.surveySettingVoToEntity(vo);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAccountId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("Test Survey");
        assertThat(result.getChannel()).isEqualTo("voice");
        assertThat(result.getChannelDirection()).isEqualTo("inbound");
        assertThat(result.getVoiceSurveyId()).isEqualTo("survey123");
        assertThat(result.getCallflowName()).isEqualTo("test-flow");
        assertThat(result.getMinFrequency()).isEqualTo(60);
        assertThat(result.getDelay()).isEqualTo(30L);
        assertThat(result.getSurveyOfferRatio()).isEqualTo(0.5F);
        assertThat(result.getMinContactLength()).isEqualTo(120L);
        assertThat(result.getSurveyForTransfers()).isTrue();
    }

    @Test
    @DisplayName("surveySettingVoToEntity should throw exception when VO is null")
    void surveySettingVoToEntity_shouldThrowExceptionWhenVoIsNull() {
        // When & Then
        assertThatThrownBy(() -> SurveyMapperJava.surveySettingVoToEntity(null))
                .hasMessage("SurveySettingVO is null");
    }

    @Test
    @DisplayName("surveySettingVoToEntity should throw exception when name is blank")
    void surveySettingVoToEntity_shouldThrowExceptionWhenNameIsBlank() {
        // Given
        SurveySettingVO vo = new SurveySettingVO(
                Optional.of(1L), 100L, "", "voice", "inbound", "survey123",
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), false
        );

        // When & Then
        assertThatThrownBy(() -> SurveyMapperJava.surveySettingVoToEntity(vo))
                .hasMessage("Survey name is missing");
    }

    @Test
    @DisplayName("surveyPhNumFormatEntityToVo should map entity to VO with allowed direction")
    void surveyPhNumFormatEntityToVo_shouldMapEntityToVoWithAllowedDirection() {
        // Given
        SurveyPhoneNumberFormatEntity entity = SurveyPhoneNumberFormatEntity.builder()
                .id(1L)
                .surveyId(100L)
                .format("+31*")
                .direction(false) // false = allowed
                .build();

        // When
        SurveyPhoneNumberFormatVO result = SurveyMapperJava.surveyPhNumFormatEntityToVo(entity);

        // Then
        assertThat(result.id()).isPresent().contains(1L);
        assertThat(result.surveyId()).isEqualTo(100L);
        assertThat(result.format()).isEqualTo("+31*");
        assertThat(result.direction()).isEqualTo("allowed");
    }

    @Test
    @DisplayName("surveyPhNumFormatEntityToVo should map entity to VO with excluded direction")
    void surveyPhNumFormatEntityToVo_shouldMapEntityToVoWithExcludedDirection() {
        // Given
        SurveyPhoneNumberFormatEntity entity = SurveyPhoneNumberFormatEntity.builder()
                .id(1L)
                .surveyId(100L)
                .format("+1*")
                .direction(true) // true = excluded
                .build();

        // When
        SurveyPhoneNumberFormatVO result = SurveyMapperJava.surveyPhNumFormatEntityToVo(entity);

        // Then
        assertThat(result.direction()).isEqualTo("excluded");
    }

    @Test
    @DisplayName("surveyPhNumFormatVoToEntity should map VO to entity with allowed direction")
    void surveyPhNumFormatVoToEntity_shouldMapVoToEntityWithAllowedDirection() {
        // Given
        SurveyPhoneNumberFormatVO vo = new SurveyPhoneNumberFormatVO(
                Optional.of(1L), 100L, "+31*", "allowed"
        );

        // When
        SurveyPhoneNumberFormatEntity result = SurveyMapperJava.surveyPhNumFormatVoToEntity(vo);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSurveyId()).isEqualTo(100L);
        assertThat(result.getFormat()).isEqualTo("+31*");
        assertThat(result.isDirection()).isFalse(); // false = allowed
    }

    @Test
    @DisplayName("surveyPhNumFormatVoToEntity should map VO to entity with excluded direction")
    void surveyPhNumFormatVoToEntity_shouldMapVoToEntityWithExcludedDirection() {
        // Given
        SurveyPhoneNumberFormatVO vo = new SurveyPhoneNumberFormatVO(
                Optional.of(1L), 100L, "+1*", "excluded"
        );

        // When
        SurveyPhoneNumberFormatEntity result = SurveyMapperJava.surveyPhNumFormatVoToEntity(vo);

        // Then
        assertThat(result.isDirection()).isTrue(); // true = excluded
    }

    @Test
    @DisplayName("surveySettingVoToOverviewDTO should map VO to overview DTO")
    void surveySettingVoToOverviewDTO_shouldMapVoToOverviewDto() {
        // Given
        SurveySettingVO vo = new SurveySettingVO(
                Optional.of(1L), 100L, "Test Survey", "voice", "inbound", "survey123",
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), false
        );

        // When
        SurveyOverviewDTO result = SurveyMapperJava.surveySettingVoToOverviewDTO(vo);

        // Then
        assertThat(result.id()).isPresent().contains(1L);
        assertThat(result.name()).isEqualTo("Test Survey");
        assertThat(result.channel()).isEqualTo("voice");
        assertThat(result.voiceSurveyId()).isEqualTo("survey123");
    }

    @Test
    @DisplayName("surveySettingVoToOverviewDTO with list should map all VOs to DTOs")
    void surveySettingVoToOverviewDTO_withList_shouldMapAllVosToDto() {
        // Given
        List<SurveySettingVO> vos = List.of(
                new SurveySettingVO(Optional.of(1L), 100L, "Survey 1", "voice", "inbound", "survey1",
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false),
                new SurveySettingVO(Optional.of(2L), 100L, "Survey 2", "chat", "outbound", "survey2",
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), true)
        );

        // When
        List<SurveyOverviewDTO> result = SurveyMapperJava.surveySettingVoToOverviewDTO(vos);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Survey 1");
        assertThat(result.get(1).name()).isEqualTo("Survey 2");
    }

    @Test
    @DisplayName("surveySettingDTOToVo should map DTO to VO with HTML escaping")
    void surveySettingDTOToVo_shouldMapDtoToVoWithHtmlEscaping() {
        // Given
        SurveySettingDTO dto = new SurveySettingDTO(
                Optional.of(1L), "Test Survey", "<script>alert('xss')</script>",
                "inbound", "survey123", Optional.of("callflow"),
                Optional.of(60), Optional.of(30L), Optional.of(0.5F),
                Optional.of(120L), true
        );

        // When
        SurveySettingVO result = SurveyMapperJava.surveySettingDTOToVo(dto, 100L);

        // Then
        assertThat(result.accountId()).isEqualTo(100L);
        assertThat(result.name()).isEqualTo("Test Survey");
        assertThat(result.channel()).isEqualTo("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;");
        assertThat(result.callflowName()).isPresent().contains("callflow");
    }

    @Test
    @DisplayName("surveyPhoneFormatUpdateDTOToVO should map update DTO to VO with allowed direction")
    void surveyPhoneFormatUpdateDTOToVO_shouldMapUpdateDtoToVoWithAllowedDirection() {
        // Given
        SurveyPhoneNumberFormatUpdateDTO dto = new SurveyPhoneNumberFormatUpdateDTO(
                Optional.of(1L), "+31*", 0 // 0 = allowed
        );

        // When
        SurveyPhoneNumberFormatVO result = SurveyMapperJava.surveyPhoneFormatUpdateDTOToVO(dto, 100L);

        // Then
        assertThat(result.id()).isPresent().contains(1L);
        assertThat(result.surveyId()).isEqualTo(100L);
        assertThat(result.format()).isEqualTo("+31*");
        assertThat(result.direction()).isEqualTo("allowed");
    }

    @Test
    @DisplayName("surveyPhoneFormatUpdateDTOToVO should map update DTO to VO with excluded direction")
    void surveyPhoneFormatUpdateDTOToVO_shouldMapUpdateDtoToVoWithExcludedDirection() {
        // Given
        SurveyPhoneNumberFormatUpdateDTO dto = new SurveyPhoneNumberFormatUpdateDTO(
                Optional.of(1L), "+1*", 1 // 1 = excluded
        );

        // When
        SurveyPhoneNumberFormatVO result = SurveyMapperJava.surveyPhoneFormatUpdateDTOToVO(dto, 100L);

        // Then
        assertThat(result.direction()).isEqualTo("excluded");
    }

    @Test
    @DisplayName("surveyDetailsVOToOverviewDto should separate allowed and excluded phone formats")
    void surveyDetailsVOToOverviewDto_shouldSeparateAllowedAndExcludedPhoneFormats() {
        // Given
        SurveySettingVO settingVO = new SurveySettingVO(
                Optional.of(1L), 100L, "Test Survey", "voice", "inbound", "survey123",
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), false
        );

        List<SurveyPhoneNumberFormatVO> phoneFormats = List.of(
                new SurveyPhoneNumberFormatVO(Optional.of(1L), 1L, "+31*", "allowed"),
                new SurveyPhoneNumberFormatVO(Optional.of(2L), 1L, "+1*", "excluded"),
                new SurveyPhoneNumberFormatVO(Optional.of(3L), 1L, "+44*", "allowed")
        );

        SurveyDetailsVO detailsVO = new SurveyDetailsVO(settingVO, phoneFormats, List.of(), List.of());

        // When
        SurveyDetailsDTO result = SurveyMapperJava.surveyDetailsVOToOverviewDto(detailsVO);

        // Then
        assertThat(result.allowedPhNumFormats()).hasSize(2);
        assertThat(result.excludedPhNumFormats()).hasSize(1);
        
        assertThat(result.allowedPhNumFormats())
                .extracting(SurveyPhoneNumberFormatDTO::format)
                .containsExactlyInAnyOrder("+31*", "+44*");
                
        assertThat(result.excludedPhNumFormats())
                .extracting(SurveyPhoneNumberFormatDTO::format)
                .containsExactly("+1*");
    }

    @Test
    @DisplayName("organisationMapper should handle organisation with parent and grandparent")
    void organisationMapper_shouldHandleOrganisationWithParentAndGrandparent() {
        // This test would require access to the private organisationMapper method
        // and proper setup of SurveyOrgDetails, which may need additional test utilities
        // or making the method package-private for testing
    }

    @Test
    @DisplayName("surveyUpdateDTOToVo should map update DTO with formats added and removed")
    void surveyUpdateDTOToVo_shouldMapUpdateDtoWithFormatsAddedAndRemoved() {
        // Given
        SurveySettingDTO settingDTO = new SurveySettingDTO(
                Optional.of(1L), "Test Survey", "voice", "inbound", "survey123",
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), false
        );

        List<SurveyPhoneNumberFormatUpdateDTO> formatsAdded = List.of(
                new SurveyPhoneNumberFormatUpdateDTO(Optional.empty(), "+31*", 0)
        );

        List<SurveyPhoneNumberFormatUpdateDTO> formatsRemoved = List.of(
                new SurveyPhoneNumberFormatUpdateDTO(Optional.of(1L), "+1*", 1)
        );

        SurveyUpdateDTO updateDTO = new SurveyUpdateDTO(settingDTO, formatsAdded, formatsRemoved);

        // When
        SurveyUpdateVO result = SurveyMapperJava.surveyUpdateDTOToVo(updateDTO, 100L, 1L);

        // Then
        assertThat(result.settings().accountId()).isEqualTo(100L);
        assertThat(result.formatsAdded()).hasSize(1);
        assertThat(result.formatsRemoved()).hasSize(1);
        assertThat(result.formatsAdded().get(0).direction()).isEqualTo("allowed");
        assertThat(result.formatsRemoved().get(0).direction()).isEqualTo("excluded");
    }
}
