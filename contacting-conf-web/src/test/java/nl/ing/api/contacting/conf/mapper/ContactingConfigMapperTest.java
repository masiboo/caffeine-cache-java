package nl.ing.api.contacting.conf.mapper;


import nl.ing.api.contacting.conf.domain.entity.cassandra.ContactingConfigEntity;
import nl.ing.api.contacting.conf.domain.model.permission.ContactingConfigVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContactingConfigMapperTest {

    @Test
    void toVO_shouldMapEntityToVO() {
        ContactingConfigEntity entity = ContactingConfigEntity.builder()
                .key("BUSINESS_FUNCTIONS")
                .values("func1,func2")
                .build();

        ContactingConfigVO vo = ContactingConfigMapper.toVO(entity);

        assertEquals("BUSINESS_FUNCTIONS", vo.key());
        assertEquals("func1,func2", vo.values());
    }

    @Test
    void toVO_shouldReturnDefaultVOForNullEntity() {
        ContactingConfigVO vo = ContactingConfigMapper.toVO(null);

        assertEquals("", vo.key());
        assertEquals("", vo.values());
    }

    @Test
    void toEntity_shouldMapVOToEntity() {
        ContactingConfigVO vo = new ContactingConfigVO("ROLES", "ADMIN,USER");
        ContactingConfigEntity entity = ContactingConfigMapper.toEntity(vo);

        assertEquals("ROLES", entity.getKey());
        assertEquals("ADMIN,USER", entity.getValues());
    }

    @Test
    void toEntity_shouldReturnNullForNullVO() {
        assertNull(ContactingConfigMapper.toEntity(null));
    }
}