package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.OrganisationSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OrganisationSettingsJpaRepository extends JpaRepository<OrganisationSettingsEntity, Long> {

    List<OrganisationSettingsEntity> findByOrgIdInAndAccountId(Set<Long> orgIds, Long accountId);

    List<OrganisationSettingsEntity> findByCapabilitiesContainingAndAccountIdOrderByOrgId(String capabilities, Long accountId);

    List<OrganisationSettingsEntity> findAllByOrderByOrgId();

    Optional<OrganisationSettingsEntity> findByIdAndAccountId(Long id, Long accountId);

}
