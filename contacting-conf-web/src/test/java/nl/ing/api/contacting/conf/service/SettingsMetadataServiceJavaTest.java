package nl.ing.api.contacting.conf.service;

import nl.ing.api.contacting.conf.domain.InputTypeJava;
import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataEntity;
import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataOptionsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataWithOptions;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsOptionsVO;
import nl.ing.api.contacting.conf.repository.SettingsMetadataOptionsJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SettingsMetadataServiceJava")
class SettingsMetadataServiceJavaTest {

    private SettingsMetadataOptionsJpaRepository repo;
    private SettingsMetadataServiceJava service;

    @BeforeEach
    void setUp() {
        repo = mock(SettingsMetadataOptionsJpaRepository.class);
        service = new SettingsMetadataServiceJava(repo);
    }

    @Nested
    @DisplayName("getSettingsMetadata")
    class GetSettingsMetadata {

        @Test
        @DisplayName("should return DTOs when repository returns data")
        void shouldReturnDTOs() {
            var metadataEntity = new SettingsMetadataEntity(
                    1L,
                    "testName",
                    InputTypeJava.RADIO,
                    "\\d+",
                    "video",
                    "customer",
                    List.of()
            );
            var optionsEntity = new SettingsMetadataOptionsEntity(
                    10L,
                    "option1",
                    "Option 1",
                    1L,
                    metadataEntity
            );
            var withOptions = new SettingsMetadataWithOptions(metadataEntity, optionsEntity);

            when(repo.findAllWithMetadata()).thenReturn(List.of(withOptions));

            var result = service.getSettingsMetadata();

            assertFalse(result.data().isEmpty());
            assertEquals("testName", result.data().get(0).name());
            assertEquals(InputTypeJava.RADIO, result.data().get(0).inputType());
        }

        @Test
        @DisplayName("should return empty DTOs when repository returns empty")
        void shouldReturnEmptyDTOs() {
            when(repo.findAllWithMetadata()).thenReturn(Collections.emptyList());
            var result = service.getSettingsMetadata();
            assertTrue(result.data().isEmpty());
        }

        @Test
        @DisplayName("should throw Errors.serverError on exception repository throws an exception")
        void shouldThrowOnException() {
            when(repo.findAllWithMetadata()).thenThrow(new RuntimeException("DB error"));
            assertThrows(RuntimeException.class, () -> service.getSettingsMetadata());
        }
    }

    @Nested
    @DisplayName("findByName")
    class FindByMetadataName {

        @Test
        @DisplayName("should return Optional.of VO when found")
        void shouldReturnVO() {
            var metadataEntity = new SettingsMetadataEntity(
                    2L,
                    "foo",
                    InputTypeJava.TEXTBOX,
                    "\\d+",
                    "chat",
                    "employee",
                    List.of()
            );
            var optionsEntity = new SettingsMetadataOptionsEntity(
                    20L,
                    "fooOption",
                    "Foo Option",
                    2L,
                    metadataEntity
            );
            var withOptions = new SettingsMetadataWithOptions(metadataEntity, optionsEntity);

            when(repo.findByMetadataName("foo")).thenReturn(List.of(withOptions));
            var result = service.findByName("foo");
            assertTrue(result.isPresent());
            assertEquals("foo", result.get().name());
        }

        @Test
        @DisplayName("should return Optional.empty when not found")
        void shouldReturnEmpty() {
            when(repo.findByMetadataName("foo")).thenReturn(Collections.emptyList());
            var result = service.findByName("foo");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw exception on db exception")
        void shouldReturnEmptyOnException() {
            when(repo.findByMetadataName("foo")).thenThrow(new RuntimeException("DB error"));
            assertThrows(RuntimeException.class, () -> service.findByName("foo"));
        }
    }

    @Nested
    @DisplayName("regexDontMatch")
    class RegexDontMatch {

        @Test
        @DisplayName("should return false if no regex present")
        void shouldReturnFalseIfNoRegex() {
            var vo = mock(SettingsMetadataVO.class);
            when(vo.regex()).thenReturn(Optional.empty());

            assertFalse(SettingsMetadataServiceJava.regexDontMatch(vo, "any"));
        }

        @Test
        @DisplayName("should return true if value does not match regex")
        void shouldReturnTrueIfNotMatch() {
            var vo = mock(SettingsMetadataVO.class);
            when(vo.regex()).thenReturn(Optional.of("\\d+"));

            assertTrue(SettingsMetadataServiceJava.regexDontMatch(vo, "abc"));
        }

        @Test
        @DisplayName("should return false if value matches regex")
        void shouldReturnFalseIfMatch() {
            var vo = mock(SettingsMetadataVO.class);
            when(vo.regex()).thenReturn(Optional.of("\\d+"));

            assertFalse(SettingsMetadataServiceJava.regexDontMatch(vo, "123"));
        }
    }

    @Nested
    @DisplayName("optionsDontMatch")
    class OptionsDontMatch {

        @Test
        @DisplayName("should return false if inputType is not validatable")
        void shouldReturnFalseIfNotValidatable() {
            var vo = mock(SettingsMetadataVO.class);
            when(vo.inputType()).thenReturn(InputTypeJava.TEXTBOX);
            when(vo.options()).thenReturn(List.of());

            assertFalse(SettingsMetadataServiceJava.optionsDontMatch(vo, "any"));
        }

        @Test
        @DisplayName("should return true if value not in options")
        void shouldReturnTrueIfValueNotInOptions() {
            var vo = mock(SettingsMetadataVO.class);
            when(vo.inputType()).thenReturn(InputTypeJava.RADIO);
            var option = mock(SettingsOptionsVO.class);
            when(option.value()).thenReturn("foo");
            when(vo.options()).thenReturn(List.of(option));

            assertTrue(SettingsMetadataServiceJava.optionsDontMatch(vo, "bar"));
        }

        @Test
        @DisplayName("should return false if value in options")
        void shouldReturnFalseIfValueInOptions() {
            var vo = mock(SettingsMetadataVO.class);
            when(vo.inputType()).thenReturn(InputTypeJava.RADIO);
            var option = mock(SettingsOptionsVO.class);
            when(option.value()).thenReturn("foo");
            when(vo.options()).thenReturn(List.of(option));

            assertFalse(SettingsMetadataServiceJava.optionsDontMatch(vo, "foo"));
        }
    }
}
