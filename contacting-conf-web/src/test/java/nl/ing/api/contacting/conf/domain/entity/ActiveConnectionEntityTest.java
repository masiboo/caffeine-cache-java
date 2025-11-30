package nl.ing.api.contacting.conf.domain.entity;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import nl.ing.api.contacting.conf.helper.ConnectionTestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActiveConnectionEntityTest {

    @Test
    public void testDummyActiveConnectionEntity() {
        ActiveConnectionEntity entity = ConnectionTestData.createDummyActiveConnectionEntity();

        assertNotNull(entity);
        assertNotNull(entity.getConnectionId());
        assertEquals(1L, entity.getConnectionId());
        assertEquals(101L, entity.getConnectionDetails().getId());

        assertNotNull(entity.getConnection());
        assertEquals("dummy.domain.com", entity.getConnection().getDomain());

        assertNotNull(entity.getConnectionDetails());
        assertEquals(ConnectionType.PRIMARY.getValue(), entity.getConnectionDetails().getConnectionType());
    }

    @Test
    public void testNoArgsConstructor() {
        ActiveConnectionEntity entity = new ActiveConnectionEntity();
        assertNull(entity.getConnection());
        assertNull(entity.getConnectionDetails());
    }
}
