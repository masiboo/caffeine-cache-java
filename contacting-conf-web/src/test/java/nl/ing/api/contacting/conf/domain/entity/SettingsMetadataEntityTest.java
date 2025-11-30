package nl.ing.api.contacting.conf.domain.entity;

import nl.ing.api.contacting.conf.domain.InputTypeJava;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SettingsMetadataEntityTest {

    @Test
    void testConstructorAndGetters() {
        SettingsMetadataOptionsEntity option = new SettingsMetadataOptionsEntity();
        List<SettingsMetadataOptionsEntity> options = List.of(option);

        SettingsMetadataEntity entity = new SettingsMetadataEntity(
                1L,
                "settingName",
                InputTypeJava.RADIO,
                "^[a-z]+$",
                "capabilityA",
                "consumerA",
                options
        );

        assertEquals(1L, entity.getId());
        assertEquals("settingName", entity.getName());
        assertEquals(InputTypeJava.RADIO, entity.getInputType());
        assertEquals("^[a-z]+$", entity.getRegex());
        assertEquals("capabilityA", entity.getCapability());
        assertEquals("consumerA", entity.getConsumers());
        assertEquals(options, entity.getOptions());
    }

    @Test
    void testSettersAndNullFields() {
        SettingsMetadataEntity entity = new SettingsMetadataEntity();
        entity.setId(2L);
        entity.setName("name2");
        entity.setInputType(InputTypeJava.DROPDOWN);
        entity.setRegex(null);
        entity.setCapability(null);
        entity.setConsumers(null);
        entity.setOptions(null);

        assertEquals(2L, entity.getId());
        assertEquals("name2", entity.getName());
        assertEquals(InputTypeJava.DROPDOWN, entity.getInputType());
        assertNull(entity.getRegex());
        assertNull(entity.getCapability());
        assertNull(entity.getConsumers());
        assertNull(entity.getOptions());
    }
}