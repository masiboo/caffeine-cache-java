package nl.ing.api.contacting.conf.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganisationSettingsEntityTest {

    @Test
    @DisplayName("Should create entity using constructor and verify getters")
    void testConstructorAndGetters() {
        OrganisationSettingsEntity entity = OrganisationSettingsEntity.builder()
                .id(1L)
                .key("settingKey")
                .value("settingValue")
                .accountId(1L)
                .enabled(true)
                .capabilities("testCapabilities")
                .build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getKey()).isEqualTo("settingKey");
        assertThat(entity.getValue()).isEqualTo("settingValue");
        assertThat(entity.getAccountId()).isEqualTo(1L);
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getCapabilities()).isEqualTo("testCapabilities");

    }

    @Test
    @DisplayName("Should create entity using setters and verify values")
    void testSetters() {
        OrganisationSettingsEntity entity = new OrganisationSettingsEntity();
        entity.setId(2L);
        entity.setKey("testKey2");
        entity.setValue("testValue2");
        entity.setCapabilities("cap1,cap2");
        entity.setAccountId(1L);
        entity.setEnabled(false);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getKey()).isEqualTo("testKey2");
        assertThat(entity.getValue()).isEqualTo("testValue2");
        assertThat(entity.getCapabilities()).isEqualTo("cap1,cap2");
        assertThat(entity.getAccountId()).isEqualTo(1L);
        assertThat(entity.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should create entity using builder and verify values")
    void testBuilder() {
        OrganisationSettingsEntity entity = OrganisationSettingsEntity.builder()
                .id(3L)
                .key("builderKey")
                .value("builderValue")
                .capabilities("builderCap")
                .accountId(1L)
                .enabled(true)
                .build();

        assertThat(entity.getId()).isEqualTo(3L);
        assertThat(entity.getKey()).isEqualTo("builderKey");
        assertThat(entity.getValue()).isEqualTo("builderValue");
        assertThat(entity.getCapabilities()).isEqualTo("builderCap");
        assertThat(entity.getAccountId()).isEqualTo(1L);
        assertThat(entity.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should handle null values for optional fields")
    void testNullableFields() {
        OrganisationSettingsEntity entity = OrganisationSettingsEntity.builder()
                .id(4L)
                .key("requiredKey")
                .value("requiredValue")
                .capabilities(null)
                .accountId(null)
                .enabled(false)
                .build();

        assertThat(entity.getId()).isEqualTo(4L);
        assertThat(entity.getKey()).isEqualTo("requiredKey");
        assertThat(entity.getValue()).isEqualTo("requiredValue");
        assertThat(entity.getCapabilities()).isNull();
        assertThat(entity.getAccountId()).isNull();
        assertThat(entity.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should verify equals and hashCode")
    void testEqualsAndHashCode() {
        OrganisationSettingsEntity entity1 = OrganisationSettingsEntity.builder()
                .id(5L)
                .key("key")
                .value("value")
                .build();

        OrganisationSettingsEntity entity2 = OrganisationSettingsEntity.builder()
                .id(5L)
                .key("key")
                .value("value")
                .build();

        OrganisationSettingsEntity entity3 = OrganisationSettingsEntity.builder()
                .id(6L)
                .key("key")
                .value("value")
                .build();

        assertThat(entity1)
                .isEqualTo(entity2)
                .hasSameHashCodeAs(entity2)
                .isNotEqualTo(entity3);
    }
}
