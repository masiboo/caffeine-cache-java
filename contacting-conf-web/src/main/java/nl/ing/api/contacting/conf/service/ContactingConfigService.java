package nl.ing.api.contacting.conf.service;

import nl.ing.api.contacting.conf.domain.entity.cassandra.ContactingConfigEntity;
import nl.ing.api.contacting.conf.mapper.ContactingConfigMapper;
import nl.ing.api.contacting.conf.repository.ContactingConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContactingConfigService {

    private final ContactingConfigRepository contactingConfigRepository;

    public ContactingConfigService(ContactingConfigRepository contactingConfigRepository) {
        this.contactingConfigRepository = contactingConfigRepository;
    }

    public Set<String> findByKey(String key) {
        return contactingConfigRepository.findByKey(key).stream()
                .flatMap(entity -> ContactingConfigMapper.toVO(entity).valuesAsSet().stream())
                .collect(Collectors.toSet());
    }

    public List<ContactingConfigEntity> findAll() {
        return contactingConfigRepository.findAll();
    }
}