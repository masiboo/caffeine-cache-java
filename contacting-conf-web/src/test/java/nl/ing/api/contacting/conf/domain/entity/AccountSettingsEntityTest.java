package nl.ing.api.contacting.conf.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountSettingsEntityTest {

    @Test
    void testConstructorAndGetters() {
        AccountSettingsEntity entity = new AccountSettingsEntity(
                1L, "testKey", "testValue", "testCapabilities", "testConsumers", 42L);

        assertEquals(1L, entity.getId());
        assertEquals("testKey", entity.getKey());
        assertEquals("testValue", entity.getValue());
        assertEquals("testCapabilities", entity.getCapabilities());
        assertEquals("testConsumers", entity.getConsumers());
        assertEquals(42L, entity.getAccountId());
    }

    @Test
    void testSettersAndNullCapabilitiesConsumers() {
        AccountSettingsEntity entity = new AccountSettingsEntity();
        entity.setId(2L);
        entity.setKey("key2");
        entity.setValue("value2");
        entity.setCapabilities(null);
        entity.setConsumers(null);
        entity.setAccountId(99L);

        assertEquals(2L, entity.getId());
        assertEquals("key2", entity.getKey());
        assertEquals("value2", entity.getValue());
        assertEquals(null, entity.getCapabilities());
        assertEquals(null, entity.getConsumers());
        assertEquals(99L, entity.getAccountId());
    }
}