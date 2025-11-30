package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.AttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeJpaRepository extends JpaRepository<AttributeEntity, Long> {
    List<AttributeEntity> findByAccountId(Long accountId);

    Optional<AttributeEntity> findByIdAndAccountId(Long id, Long accountId);
}