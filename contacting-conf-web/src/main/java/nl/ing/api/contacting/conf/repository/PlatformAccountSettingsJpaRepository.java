package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.PlatformAccountSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformAccountSettingsJpaRepository extends JpaRepository<PlatformAccountSettingsEntity, Long> {
    List<PlatformAccountSettingsEntity> findByAccountId(Long accountId);
}
