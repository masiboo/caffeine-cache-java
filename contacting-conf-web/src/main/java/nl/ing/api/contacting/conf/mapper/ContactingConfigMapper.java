package nl.ing.api.contacting.conf.mapper;

import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.cassandra.ContactingConfigEntity;
import nl.ing.api.contacting.conf.domain.model.permission.ContactingConfigVO;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ContactingConfigMapper {

public static ContactingConfigVO toVO(ContactingConfigEntity entity) {
    return entity == null
        ? new ContactingConfigVO("", "")
        : new ContactingConfigVO(entity.getKey(), entity.getValues());
}
    public static ContactingConfigEntity toEntity(ContactingConfigVO vo) {
        if (vo == null) return null;
        return ContactingConfigEntity.builder()
                .key(vo.key())
                .values(vo.values())
                .build();
    }
}