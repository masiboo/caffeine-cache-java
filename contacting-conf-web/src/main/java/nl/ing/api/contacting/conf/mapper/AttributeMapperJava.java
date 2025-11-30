package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.attribute.AttributeDto;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.AttributeEntity;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AttributeMapperJava {
    /**
     * Maps an {@link AttributeEntity} to an {@link AttributeDto}.
     * <p>
     * Converts all relevant fields, using Optionals for nullable values.
     *
     * @param attributeVO the source entity to map from
     * @return the mapped {@link AttributeDto}
     */
    public static AttributeDto toDTO(AttributeEntity attributeVO) {
        return new AttributeDto(
                attributeVO.getIdOptional(),
                attributeVO.getEntity(),
                attributeVO.getEntityValue(),
                attributeVO.getLabel(),
                attributeVO.getLabelValue(),
                attributeVO.getLabelContentOptional(),
                attributeVO.getDisplayOrderOptional()
        );
    }

    /**
     * Converts the AttributeDto to AttributeVO object.
     * @param attributeDto the DTO to be converted to VO
     * @param context the context of the attribute
     * @return the attribute VO object
     */
    public static AttributeEntity toEntity(AttributeDto attributeDto, ContactingContext context) {
        return new AttributeEntity(
                attributeDto.id().orElse(null),
                attributeDto.entity(),
                attributeDto.entityValue(),
                attributeDto.label(),
                attributeDto.labelValue(),
                attributeDto.labelContent().orElse(""),
                attributeDto.displayOrder().orElse(null),
                context.accountId());
    }

}
