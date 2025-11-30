package nl.ing.api.contacting.conf.resource;

import jakarta.ws.rs.core.Response;
import nl.ing.api.contacting.conf.domain.InputTypeJava;
import nl.ing.api.contacting.conf.resource.dto.SettingsMetadataJavaDTO;
import nl.ing.api.contacting.conf.resource.dto.SettingsMetadataJavaDTOs;
import nl.ing.api.contacting.conf.resource.dto.SettingsOptionsJavaDTO;
import nl.ing.api.contacting.conf.service.SettingsMetadataServiceJava;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsMetadataResourceJavaTest {

    @Mock
    private SettingsMetadataServiceJava settingsMetadataService;

    private SettingsMetadataResourceJava resource;
    private static final String SERVICE_ERROR_MESSAGE = "Service error";

    // Predefined test data
    private static final SettingsMetadataJavaDTO TIMEZONE_METADATA = createTestMetadataDTO("TIMEZONE");
    private static final SettingsMetadataJavaDTO LANGUAGE_METADATA = createTestMetadataDTO("LANGUAGE");
    private static final SettingsMetadataJavaDTO NOTIFICATION_METADATA = createTestMetadataDTO("NOTIFICATION_PREFERENCE");

    @BeforeEach
    void setUp() {
        resource = new SettingsMetadataResourceJava(settingsMetadataService);
    }

    @Nested
    @DisplayName("getAllSettings")
    class GetAllSettings {

        @Test
        @DisplayName("returns single metadata entry")
        void returnsSingleMetadata() throws Exception {
            // Given
            SettingsMetadataJavaDTOs metadataDTOs = new SettingsMetadataJavaDTOs(List.of(TIMEZONE_METADATA));
            when(settingsMetadataService.getSettingsMetadata())
                    .thenReturn(metadataDTOs);

            // When
            Response response = resource.getAllSettings().get();

            // Then
            assertEquals(200, response.getStatus());
            SettingsMetadataJavaDTOs result = (SettingsMetadataJavaDTOs) response.getEntity();
            assertEquals(1, result.data().size());
            assertEquals("TIMEZONE", result.data().get(0).name());
        }

        @Test
        @DisplayName("returns multiple metadata entries")
        void returnsMultipleMetadata() throws Exception {
            // Given
            SettingsMetadataJavaDTOs metadataDTOs = new SettingsMetadataJavaDTOs(
                    List.of(TIMEZONE_METADATA, LANGUAGE_METADATA, NOTIFICATION_METADATA));
            when(settingsMetadataService.getSettingsMetadata())
                    .thenReturn(metadataDTOs);

            // When
            Response response = resource.getAllSettings().get();

            // Then
            assertEquals(200, response.getStatus());
            SettingsMetadataJavaDTOs result = (SettingsMetadataJavaDTOs) response.getEntity();
            assertEquals(3, result.data().size());
            assertEquals("TIMEZONE", result.data().get(0).name());
            assertEquals("LANGUAGE", result.data().get(1).name());
            assertEquals("NOTIFICATION_PREFERENCE", result.data().get(2).name());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("nl.ing.api.contacting.conf.resource.SettingsMetadataResourceJavaTest#edgeCaseScenarios")
        @DisplayName("handles edge cases")
        void handlesEdgeCases(String testName, SettingsMetadataJavaDTOs serviceResponse,
                              boolean hasEntity) throws Exception {
            // Given
            when(settingsMetadataService.getSettingsMetadata()).thenReturn(serviceResponse);

            // When
            Response response = resource.getAllSettings().get();

            // Then
            assertEquals(200, response.getStatus());
            if (hasEntity) {
                var entity = response.getEntity();
                assertNotNull(entity);
                var result = (SettingsMetadataJavaDTOs) entity;
                assertTrue(result.data().isEmpty());
            } else {
                assertNull(response.getEntity());
            }
        }

        @Test
        @DisplayName("handles service exception")
        void handlesServiceException() throws Exception {
            when(settingsMetadataService.getSettingsMetadata())
                    .thenThrow(new RuntimeException(SERVICE_ERROR_MESSAGE));
            Response response = resource.getAllSettings().get();
            assertEquals(500, response.getStatus());
            assertTrue(response.getEntity().toString().contains("An unexpected error occurred. Please try again later."));
        }
    }

    // Test data factory methods
    private static SettingsMetadataJavaDTO createTestMetadataDTO(String name) {
        return new SettingsMetadataJavaDTO(
                name,
                InputTypeJava.TEXTBOX,
                Optional.of(".*"),
                List.of(new SettingsOptionsJavaDTO("option1", "Option 1")),
                List.of("video"),
                List.of("customer")
        );
    }

    // Data provider for parameterized tests
    static Stream<Arguments> edgeCaseScenarios() {
        return Stream.of(
                Arguments.of(
                        "Empty list",
                        new SettingsMetadataJavaDTOs(Collections.emptyList()),
                        true
                ),
                Arguments.of(
                        "Null result",
                        null,
                        false
                )
        );
    }
}
