package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.PlatformAccountSetting;
import nl.ing.api.contacting.conf.domain.entity.PlatformAccountSettingsEntity;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlatformAccountSettingsMapperTest {

    @Test
    void shouldMapEntityToVO() {
        PlatformAccountSettingsEntity entity = new PlatformAccountSettingsEntity();
        entity.setId(100L);
        entity.setKey("apiKey");
        entity.setValue("apiValue");
        entity.setAccountId(101L);
        PlatformAccountSetting vo = PlatformAccountSettingsMapper.toPlatformAccountSetting(entity);
        assertEquals(Optional.of(100L), vo.id());
        assertEquals("apiKey", vo.key());
        assertEquals("apiValue", vo.value());
        assertEquals(101L, vo.accountId());
    }

    @Test
    void shouldHandleNullId() {
        PlatformAccountSettingsEntity entity = new PlatformAccountSettingsEntity();
        entity.setId(null);
        entity.setKey("k");
        entity.setValue("v");
        entity.setAccountId(101L);

        PlatformAccountSetting vo = PlatformAccountSettingsMapper.toPlatformAccountSetting(entity);
        assertEquals(Optional.empty(), vo.id(), "VO id should be Optional.empty() when entity id is null");
        assertEquals("k", vo.key());
        assertEquals("v", vo.value());
        assertEquals(101L, vo.accountId());

    }
}