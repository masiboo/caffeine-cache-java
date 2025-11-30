package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.cassandra.ContactingConfigEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactingConfigRepository extends CassandraRepository<ContactingConfigEntity, String> {
    List<ContactingConfigEntity> findByKey(String key);
}