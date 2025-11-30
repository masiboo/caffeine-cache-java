package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.InputTypeJava;
import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataEntity;
import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataOptionsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataWithOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("SettingsMetadataOptionsJpaRepository Tests")
@EntityScan("nl.ing.api.contacting.conf.domain.entity")
class SettingsMetadataOptionsJpaRepositoryTest extends BaseJpaTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SettingsMetadataOptionsJpaRepository settingsMetadataOptionsJpaRepository;

    private SettingsMetadataEntity themesMetadata;
    private SettingsMetadataEntity languageMetadata;
    private SettingsMetadataEntity timezoneMetadata;
    private SettingsMetadataOptionsEntity darkThemeOption;
    private SettingsMetadataOptionsEntity lightThemeOption;
    private SettingsMetadataOptionsEntity englishOption;
    private SettingsMetadataOptionsEntity dutchOption;
    private SettingsMetadataOptionsEntity utcOption;

    @BeforeEach
    void setUp() {
        // Create metadata entities
        themesMetadata = SettingsMetadataEntity.builder()
                .name("platform.theme")
                .inputType(InputTypeJava.RADIO)
                .capability("UI")
                .consumers("web,mobile")
                .build();

        languageMetadata = SettingsMetadataEntity.builder()
                .name("platform.language")
                .inputType(InputTypeJava.DROPDOWN)
                .capability("LOCALIZATION")
                .consumers("web,mobile")
                .build();

        timezoneMetadata = SettingsMetadataEntity.builder()
                .name("platform.timezone")
                .inputType(InputTypeJava.DROPDOWN)
                .capability("TIME")
                .consumers("web")
                .build();

        entityManager.persistAndFlush(themesMetadata);
        entityManager.persistAndFlush(languageMetadata);
        entityManager.persistAndFlush(timezoneMetadata);

        // Create options for themes
        darkThemeOption = SettingsMetadataOptionsEntity.builder()
                .settingsMetaId(themesMetadata.getId())
                .value("dark")
                .displayName("Dark Theme")
                .build();

        lightThemeOption = SettingsMetadataOptionsEntity.builder()
                .settingsMetaId(themesMetadata.getId())
                .value("light")
                .displayName("Light Theme")
                .build();

        // Create options for language
        englishOption = SettingsMetadataOptionsEntity.builder()
                .settingsMetaId(languageMetadata.getId())
                .value("en")
                .displayName("English")
                .build();

        dutchOption = SettingsMetadataOptionsEntity.builder()
                .settingsMetaId(languageMetadata.getId())
                .value("nl")
                .displayName("Dutch")
                .build();

        // Create option for timezone
        utcOption = SettingsMetadataOptionsEntity.builder()
                .settingsMetaId(timezoneMetadata.getId())
                .value("UTC")
                .displayName("UTC Timezone")
                .build();

        entityManager.persistAndFlush(darkThemeOption);
        entityManager.persistAndFlush(lightThemeOption);
        entityManager.persistAndFlush(englishOption);
        entityManager.persistAndFlush(dutchOption);
        entityManager.persistAndFlush(utcOption);
    }

    @Test
    @DisplayName("should find all metadata with options")
    void shouldFindAllWithMetadata() {
        List<SettingsMetadataWithOptions> allWithOptions = settingsMetadataOptionsJpaRepository.findAllWithMetadata();

        assertThat(allWithOptions).hasSize(5);

        // Verify we have results for all three metadata entries
        List<String> metadataNames = allWithOptions.stream()
                .map(item -> item.metadata().getName())
                .distinct()
                .toList();

        assertThat(metadataNames).containsExactlyInAnyOrder(
                "platform.theme", "platform.language", "platform.timezone"
        );

        // Verify theme metadata has 2 options
        List<SettingsMetadataWithOptions> themeOptions = allWithOptions.stream()
                .filter(item -> "platform.theme".equals(item.metadata().getName()))
                .toList();

        assertThat(themeOptions).hasSize(2);
        assertThat(themeOptions)
                .extracting(item -> item.option().getValue())
                .containsExactlyInAnyOrder("dark", "light");
    }

    @Test
    @DisplayName("should find metadata with options by name")
    void shouldFindByMetadataName() {
        List<SettingsMetadataWithOptions> languageOptions = settingsMetadataOptionsJpaRepository
                .findByMetadataName("platform.language");

        assertThat(languageOptions).hasSize(2);

        // Verify all results are for language metadata
        assertThat(languageOptions)
                .extracting(item -> item.metadata().getName())
                .containsOnly("platform.language");

        // Verify option values
        assertThat(languageOptions)
                .extracting(item -> item.option().getValue())
                .containsExactlyInAnyOrder("en", "nl");

        // Verify display names
        assertThat(languageOptions)
                .extracting(item -> item.option().getDisplayName())
                .containsExactlyInAnyOrder("English", "Dutch");
    }

    @Test
    @DisplayName("should find single option by metadata name")
    void shouldFindSingleOptionByMetadataName() {
        List<SettingsMetadataWithOptions> timezoneOptions = settingsMetadataOptionsJpaRepository
                .findByMetadataName("platform.timezone");

        assertThat(timezoneOptions).hasSize(1);

        SettingsMetadataWithOptions timezoneOption = timezoneOptions.get(0);
        assertThat(timezoneOption.metadata().getName()).isEqualTo("platform.timezone");
        assertThat(timezoneOption.option().getValue()).isEqualTo("UTC");
        assertThat(timezoneOption.option().getDisplayName()).isEqualTo("UTC Timezone");
    }

    @Test
    @DisplayName("should return empty list when finding by non-existing metadata name")
    void shouldReturnEmptyListWhenFindingByNonExistingMetadataName() {
        List<SettingsMetadataWithOptions> nonExistingOptions = settingsMetadataOptionsJpaRepository
                .findByMetadataName("platform.nonexisting");

        assertThat(nonExistingOptions).isEmpty();
    }

    @Test
    @DisplayName("should verify metadata properties in results")
    void shouldVerifyMetadataPropertiesInResults() {
        List<SettingsMetadataWithOptions> themeResults = settingsMetadataOptionsJpaRepository
                .findByMetadataName("platform.theme");

        assertThat(themeResults).hasSize(2);

        SettingsMetadataWithOptions firstResult = themeResults.get(0);
        SettingsMetadataEntity metadata = firstResult.metadata();

        assertThat(metadata.getName()).isEqualTo("platform.theme");
        assertThat(metadata.getInputType()).isEqualTo(InputTypeJava.RADIO);
        assertThat(metadata.getCapability()).isEqualTo("UI");
        assertThat(metadata.getConsumers()).isEqualTo("web,mobile");
    }

    @Test
    @DisplayName("should save and retrieve new option entity")
    void shouldSaveAndRetrieveNewOptionEntity() {
        SettingsMetadataOptionsEntity newOption = SettingsMetadataOptionsEntity.builder()
                .settingsMetaId(themesMetadata.getId())
                .value("system")
                .displayName("System Theme")
                .build();

        SettingsMetadataOptionsEntity saved = settingsMetadataOptionsJpaRepository.save(newOption);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getValue()).isEqualTo("system");
        assertThat(saved.getDisplayName()).isEqualTo("System Theme");

        Optional<SettingsMetadataOptionsEntity> retrieved = settingsMetadataOptionsJpaRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getSettingsMetaId()).isEqualTo(themesMetadata.getId());
    }

    @Test
    @DisplayName("should delete option entity by ID")
    void shouldDeleteOptionEntityById() {
        Long optionId = darkThemeOption.getId();

        settingsMetadataOptionsJpaRepository.deleteById(optionId);

        Optional<SettingsMetadataOptionsEntity> deleted = settingsMetadataOptionsJpaRepository.findById(optionId);
        assertThat(deleted).isEmpty();

        // Verify other options still exist
        List<SettingsMetadataWithOptions> remainingThemeOptions = settingsMetadataOptionsJpaRepository
                .findByMetadataName("platform.theme");
        assertThat(remainingThemeOptions).hasSize(1);
        assertThat(remainingThemeOptions.get(0).option().getValue()).isEqualTo("light");
    }
}
