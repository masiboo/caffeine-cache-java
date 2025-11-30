package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.attribute.AttributeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.AttributeEntity;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.AttributeMapperJava;
import nl.ing.api.contacting.conf.repository.AttributeCacheRepository;
import nl.ing.api.contacting.conf.repository.AttributeJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeJpaRepository repo;
    private final AttributeCacheRepository attributeCacheRepository;

    public List<AttributeDto> getAll(ContactingContext contactingContext) {
        return attributeCacheRepository
                .findAllForAccount(contactingContext)
                .stream()
                .map(AttributeMapperJava::toDTO)
                .toList();
    }

    public AttributeDto findById(Long attributeId, ContactingContext context) {
        var maybeAtt = attributeCacheRepository.findById(attributeId, context);
        if (maybeAtt.isEmpty())
            throw Errors.notFound(String.format("attribute with id %s not found", attributeId));
        return AttributeMapperJava.toDTO(maybeAtt.get());
    }

    public Long save(AttributeDto attributeDto, ContactingContext context) {
        AttributeEntity attEntity = AttributeMapperJava.toEntity(attributeDto, context);
        return repo.save(attEntity).getId();
    }

    @Transactional
    public void deleteById(Long attributeId, ContactingContext contactingContext) {
        log.debug("Deleting attribute with id {}", attributeId);

        repo.findByIdAndAccountId(attributeId, contactingContext.accountId())
                .ifPresentOrElse(
                        repo::delete,
                        () -> {
                            throw Errors.notFound("Attribute resource not found");
                        });
    }

    @Transactional
    public AttributeDto update(Long id, AttributeDto attributeDto, ContactingContext contactingContext) {
        AttributeEntity savedAtt = updateAttribute(id, attributeDto, contactingContext);
        return AttributeMapperJava.toDTO(savedAtt);
    }

    @Transactional
    public void updateAttributes(List<AttributeDto> attributeDtos, ContactingContext contactingContext) {
        if (attributeDtos.stream().anyMatch(dto -> dto.id().isEmpty())){
            throw Errors.badRequest("Attribute id missing for update");
        }

        attributeDtos
                .forEach(dto -> updateAttribute(dto.id().orElse(0L), dto, contactingContext));
    }

    public AttributeEntity updateAttribute(Long id, AttributeDto attributeDto, ContactingContext contactingContext) {
        Optional<Long> attributeDtoId = attributeDto.id();
        if (attributeDtoId.isEmpty()) {
            throw Errors.badRequest("Attribute id missing for update");
        }
        repo.findByIdAndAccountId(id, contactingContext.accountId())
            .orElseThrow(() -> Errors.notFound("attribute not found"));

        AttributeEntity attributeEntity = AttributeMapperJava.toEntity(attributeDto, contactingContext);
        attributeEntity.setId(id);
        return repo.save(attributeEntity);
    }


}
