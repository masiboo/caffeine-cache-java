package nl.ing.api.contacting.conf.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SettingsMetadataOptionsEntityTest {

    @Test
    void testConstructorAndGetters() {
        SettingsMetadataEntity metaEntity = new SettingsMetadataEntity();
        SettingsMetadataOptionsEntity option = new SettingsMetadataOptionsEntity(
                1L,
                "optionValue",
                "Option Display",
                10L,
                metaEntity
        );

        assertEquals(1L, option.getId());
        assertEquals("optionValue", option.getValue());
        assertEquals("Option Display", option.getDisplayName());
        assertEquals(10L, option.getSettingsMetaId());
        assertEquals(metaEntity, option.getSettingsMetaData());
    }

    @Test
    void testSettersAndNullMetaData() {
        SettingsMetadataOptionsEntity option = new SettingsMetadataOptionsEntity();
        option.setId(2L);
        option.setValue("val2");
        option.setDisplayName("Display2");
        option.setSettingsMetaId(20L);
        option.setSettingsMetaData(null);

        assertEquals(2L, option.getId());
        assertEquals("val2", option.getValue());
        assertEquals("Display2", option.getDisplayName());
        assertEquals(20L, option.getSettingsMetaId());
        assertNull(option.getSettingsMetaData());
    }
}