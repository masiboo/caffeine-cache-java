package nl.ing.api.contacting.conf.mapper;

import nl.ing.api.contacting.conf.domain.InputTypeJava;
import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataEntity;
import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataOptionsEntity;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingConsumers;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataWithOptions;
import nl.ing.api.contacting.conf.exception.ApplicationEsperantoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SettingsMetadataMapperJava")
class SettingsMetadataMapperJavaTest {


    @Test
    @DisplayName("shouldMapSingleMetadataWithOptions")
    void shouldMapSingleMetadataWithOptions() {
        SettingsMetadataEntity metadata = SettingsMetadataEntity.builder()
                .id(1L)
                .name("Test Metadata")
                .inputType(InputTypeJava.RADIO)
                .regex("\\d+")
                .capability("VIDEO")
                .consumers("CUSTOMER,API")
                .build();

        SettingsMetadataOptionsEntity option1 = SettingsMetadataOptionsEntity.builder()
                .id(10L)
                .value("opt1")
                .displayName("Option 1")
                .settingsMetaData(metadata)
                .build();

        SettingsMetadataOptionsEntity option2 = SettingsMetadataOptionsEntity.builder()
                .id(11L)
                .value("opt2")
                .displayName("Option 2")
                .settingsMetaData(metadata)
                .build();

        SettingsMetadataWithOptions swo1 = new SettingsMetadataWithOptions(metadata, option2);
        SettingsMetadataWithOptions swo2 = new SettingsMetadataWithOptions(metadata, option1);

        List<SettingsMetadataWithOptions> input = List.of(swo1, swo2);

        List<SettingsMetadataVO> result = SettingsMetadataMapperJava.metadataToVO(input);

        assertEquals(1, result.size());
        SettingsMetadataVO vo = result.get(0);
        assertEquals(metadata.getName(), vo.name());
        assertEquals(metadata.getInputType(), vo.inputType());
        assertEquals(Optional.of(metadata.getId()), vo.id());
        assertEquals(Optional.of(metadata.getRegex()), vo.regex());
        assertEquals(2, vo.options().size());
        assertEquals("Option 1", vo.options().get(0).displayName());
        assertEquals("Option 2", vo.options().get(1).displayName());
        assertEquals(List.of(AccountSettingConsumers.CUSTOMER, AccountSettingConsumers.API), vo.consumers());
        assertEquals(List.of(SettingCapability.VIDEO), vo.capability());
    }

    @Test
    @DisplayName("shouldHandleNullOption")
    void shouldHandleNullOption() {
        SettingsMetadataEntity metadata = SettingsMetadataEntity.builder()
                .id(1L)
                .name("Metadata With Null Option")
                .inputType(InputTypeJava.RADIO)
                .build();

        SettingsMetadataWithOptions swo = new SettingsMetadataWithOptions(metadata, null);

        List<SettingsMetadataVO> result = SettingsMetadataMapperJava.metadataToVO(List.of(swo));

        assertEquals(1, result.size());
        assertTrue(result.get(0).options().isEmpty());
    }

    @Test
    @DisplayName("shouldHandleNullConsumersAndCapability")
    void shouldHandleNullConsumersAndCapability() {
        SettingsMetadataEntity metadata = SettingsMetadataEntity.builder()
                .id(2L)
                .name("Metadata Without Consumers")
                .inputType(InputTypeJava.RADIO)
                .consumers(null)
                .capability(null)
                .build();

        SettingsMetadataWithOptions swo = new SettingsMetadataWithOptions(metadata, null);

        List<SettingsMetadataVO> result = SettingsMetadataMapperJava.metadataToVO(List.of(swo));

        SettingsMetadataVO vo = result.get(0);
        assertEquals(List.of(), vo.consumers());
        assertEquals(List.of(), vo.capability());
    }

    @Test
    @DisplayName("shouldSortMetadataAlphabeticallyByName")
    void shouldSortMetadataAlphabeticallyByName() {
        SettingsMetadataEntity metadataA = SettingsMetadataEntity.builder()
                .id(1L)
                .name("Alpha")
                .inputType(InputTypeJava.RADIO)
                .build();

        SettingsMetadataEntity metadataB = SettingsMetadataEntity.builder()
                .id(2L)
                .name("beta")
                .inputType(InputTypeJava.RADIO)
                .build();

        SettingsMetadataWithOptions swoA = new SettingsMetadataWithOptions(metadataA, null);
        SettingsMetadataWithOptions swoB = new SettingsMetadataWithOptions(metadataB, null);

        List<SettingsMetadataVO> result = SettingsMetadataMapperJava.metadataToVO(List.of(swoB, swoA));

        assertEquals(2, result.size());
        assertEquals("Alpha", result.get(0).name());
        assertEquals("beta", result.get(1).name());
    }

    @Test
    @DisplayName("shouldSortOptionsAlphabeticallyByDisplayName")
    void shouldSortOptionsAlphabeticallyByDisplayName() {
        SettingsMetadataEntity metadata = SettingsMetadataEntity.builder()
                .id(1L)
                .name("Metadata")
                .inputType(InputTypeJava.RADIO)
                .build();

        SettingsMetadataOptionsEntity optB = SettingsMetadataOptionsEntity.builder()
                .displayName("Bravo")
                .value("B")
                .settingsMetaData(metadata)
                .build();

        SettingsMetadataOptionsEntity optA = SettingsMetadataOptionsEntity.builder()
                .displayName("Alpha")
                .value("A")
                .settingsMetaData(metadata)
                .build();

        SettingsMetadataWithOptions swo1 = new SettingsMetadataWithOptions(metadata, optB);
        SettingsMetadataWithOptions swo2 = new SettingsMetadataWithOptions(metadata, optA);

        List<SettingsMetadataVO> result = SettingsMetadataMapperJava.metadataToVO(List.of(swo1, swo2));

        SettingsMetadataVO vo = result.get(0);
        assertEquals(2, vo.options().size());
        assertEquals("Alpha", vo.options().get(0).displayName());
        assertEquals("Bravo", vo.options().get(1).displayName());
    }

    @Test
    @DisplayName("shouldThrowExceptionForInvalidConsumerValue")
    void shouldThrowExceptionForInvalidConsumerValue() {
        SettingsMetadataEntity metadata = SettingsMetadataEntity.builder()
                .id(1L)
                .name("Metadata with Invalid Consumer")
                .inputType(InputTypeJava.RADIO)
                .consumers("INVALID_CONSUMER")
                .build();

        SettingsMetadataWithOptions swo = new SettingsMetadataWithOptions(metadata, null);
        List<SettingsMetadataWithOptions> input = List.of(swo);

        ApplicationEsperantoException ex = assertThrows(ApplicationEsperantoException.class,
                () -> SettingsMetadataMapperJava.metadataToVO(input));

        assertTrue(ex.getMessage().contains("Unknown AccountSettingConsumer value"));
    }
}
