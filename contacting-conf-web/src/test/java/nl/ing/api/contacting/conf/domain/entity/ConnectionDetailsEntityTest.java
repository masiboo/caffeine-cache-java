package nl.ing.api.contacting.conf.domain.entity;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import nl.ing.api.contacting.conf.helper.ConnectionTestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionDetailsEntityTest {

    @Test
    public void testConnectionDetailsEntityWithDummyData() {
        ConnectionDetailsEntity entity = ConnectionTestData.createConnectionDetailsEntity();

        assertNotNull(entity);
        assertEquals(101L, entity.getId());
        assertEquals(ConnectionType.WORK_FROM_HOME.getValue(), entity.getConnectionType());
        assertEquals("AMS1", entity.getEdgeLocation());
        assertEquals("https://api.test.com", entity.getUrl());
        assertEquals("EU", entity.getRegion());

        assertNotNull(entity.getConnection());
        assertEquals("dummy.domain.com", entity.getConnection().getDomain());
    }

    @Test
    public void testNoArgsConstructor() {
        ConnectionDetailsEntity entity = new ConnectionDetailsEntity();

        assertNull(entity.getId());
        assertNull(entity.getConnection());
        assertNull(entity.getConnectionType());
        assertNull(entity.getEdgeLocation());
        assertNull(entity.getUrl());
        assertNull(entity.getRegion());
    }

    @Test
    public void testBuilderPattern() {
        ConnectionEntity connection = ConnectionTestData.createDummyConnectionEntity();

        ConnectionDetailsEntity entity = ConnectionDetailsEntity.builder()
                .id(202L)
                .connection(connection)
                .connectionType(ConnectionType.WORK_FROM_HOME.getValue())
                .edgeLocation("FRA1")
                .url("https://soap.api.com")
                .region("EU")
                .build();

        assertEquals(202L, entity.getId());
        assertEquals(ConnectionType.WORK_FROM_HOME.getValue(), entity.getConnectionType());
        assertEquals("FRA1", entity.getEdgeLocation());
        assertEquals("https://soap.api.com", entity.getUrl());
        assertEquals("EU", entity.getRegion());
        assertEquals(connection, entity.getConnection());
    }
}