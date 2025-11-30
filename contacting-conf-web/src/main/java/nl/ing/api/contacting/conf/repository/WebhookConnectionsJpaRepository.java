package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.WebhookConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Repository interface for managing webhook connections in the Contacting Configuration API.
 * @author Ajit Singh
 * @since 2025-10-10
 */
@Repository
public interface WebhookConnectionsJpaRepository extends JpaRepository<WebhookConnectionEntity, Long> {
    List<WebhookConnectionEntity> findByAccountId(Long accountId);

    @Modifying
    @Query("UPDATE WebhookConnectionEntity wc SET wc.active = true WHERE wc.id IN :activeIds")
    void activateConnections(@Param("activeIds") Set<Long> activeIds);

    @Modifying
    @Query("UPDATE WebhookConnectionEntity wc SET wc.active = false WHERE wc.id NOT IN :activeIds")
    void deactivateConnections(@Param("activeIds") Set<Long> activeIds);

}
