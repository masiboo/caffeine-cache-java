package nl.ing.api.contacting.conf.domain.entity;

import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataWithOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsMetadataWithOptionsTest {

    @Test
    void testConstructorAndGetters() {
        SettingsMetadataEntity metadata = new SettingsMetadataEntity();
        SettingsMetadataOptionsEntity option = new SettingsMetadataOptionsEntity();

        SettingsMetadataWithOptions withOptions = new SettingsMetadataWithOptions(metadata, option);

        assertEquals(metadata, withOptions.metadata());
        assertEquals(option, withOptions.option());
    }
}