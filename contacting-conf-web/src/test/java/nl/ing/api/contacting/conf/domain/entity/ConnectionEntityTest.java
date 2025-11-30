package nl.ing.api.contacting.conf.domain.entity;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import nl.ing.api.contacting.conf.helper.ConnectionTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionEntityTest {

    @Test
    @DisplayName("testDummyConnectionEntity (Scala: testDummyConnectionEntity)")
    public void testDummyConnectionEntity() {
        ConnectionEntity entity = ConnectionTestData.createDummyConnectionEntity();
        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals(9999L, entity.getAccountId());
        assertEquals("dummy.domain.com", entity.getDomain());
        assertEquals(1, entity.getDetails().size());
        ConnectionDetailsEntity details = entity.getDetails().get(0);
        assertEquals(101L, details.getId());
        assertEquals(ConnectionType.PRIMARY.getValue(), details.getConnectionType());
        assertEquals("https://dummy.api.com", details.getUrl());
        assertEquals(entity, details.getConnection());
    }

    @Test
    @DisplayName("testBuilderPattern (Scala: testBuilderPattern)")
    public void testBuilderPattern() {
        ConnectionEntity entity = ConnectionTestData.createDummyConnectionEntity();
        assertEquals(1L, entity.getId());
        assertEquals(9999L, entity.getAccountId());
        assertEquals("dummy.domain.com", entity.getDomain());
        assertFalse(entity.getDetails().isEmpty());
    }
}
