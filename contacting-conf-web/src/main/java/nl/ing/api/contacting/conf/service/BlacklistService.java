package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistFunctionality;
import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.BlacklistEntity;
import nl.ing.api.contacting.conf.domain.model.blacklist.BlacklistItemVO;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.BlacklistItemMapperJava;
import nl.ing.api.contacting.conf.repository.BlacklistJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistJpaRepository repository;

    @Transactional(readOnly = true)
    public List<BlacklistItemDto> getAllBlacklistItems(ContactingContext contactingContext) {
        return toDtoList(repository.findActiveByAccount(contactingContext.accountId(), LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public List<BlacklistItemDto> getAllByFunctionality(String functionality, ContactingContext context) {

        BlacklistFunctionality func = BlacklistFunctionality.fromString(functionality)
                .orElseThrow(() -> Errors.notFound(functionality + " is not defined"));

        LocalDateTime now = LocalDateTime.now();
        List<BlacklistEntity> entities = (func == BlacklistFunctionality.ALL)
                ? repository.findActiveByAccount(context.accountId(), now)
                : repository.findByAccountIdAndFunctionalityAndActive(context.accountId(), func.value(), now);
        return toDtoList(entities);
    }

    private List<BlacklistItemDto> toDtoList(List<BlacklistEntity> entities) {
        return entities.stream()
                .map(BlacklistItemMapperJava::toVO)
                .map(BlacklistItemMapperJava::toDto)
                .toList();
    }


    @Transactional
    public BlacklistItemVO createBlackListItem(BlacklistItemVO vo, ContactingContext contactingContext) {
        BlacklistEntity entityToSave = BlacklistItemMapperJava.toEntity(vo, contactingContext.accountId());
        BlacklistEntity saved = repository.save(entityToSave);
        return BlacklistItemMapperJava.toVO(saved);
    }

    @Transactional
    public BlacklistItemVO updateBlackListItem(BlacklistItemVO vo, ContactingContext contactingContext) {
        BlacklistEntity entity = BlacklistItemMapperJava.toEntity(vo, contactingContext.accountId());
        BlacklistEntity updated = repository.save(entity);
        return BlacklistItemMapperJava.toVO(updated);
    }

    @Transactional
    public void deleteBlackListItem(Long id) {
        repository.deleteById(id);
    }
}
