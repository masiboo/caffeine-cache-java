package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.WebhookConnectionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("WebhookConnectionsJpaRepository Tests")
class WebhookConnectionsJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WebhookConnectionsJpaRepository webhookConnectionsJpaRepository;

    private WebhookConnectionEntity primaryWebhook1;
    private WebhookConnectionEntity fallbackWebhook1;
    private WebhookConnectionEntity primaryWebhook2;
    private WebhookConnectionEntity inactiveWebhook1;
    private WebhookConnectionEntity primaryWebhook3;

    @BeforeEach
    void setUp() {
        // Account 1 webhooks
        primaryWebhook1 = WebhookConnectionEntity.builder()
                .connectionType("PRIMARY")
                .url("https://webhook1.example.com/primary")
                .accountId(100L)
                .active(true)
                .build();

        fallbackWebhook1 = WebhookConnectionEntity.builder()
                .connectionType("FALLBACK")
                .url("https://webhook1.example.com/fallback")
                .accountId(100L)
                .active(false)
                .build();

        inactiveWebhook1 = WebhookConnectionEntity.builder()
                .connectionType("SECONDARY")
                .url("https://webhook1.example.com/secondary")
                .accountId(100L)
                .active(false)
                .build();

        // Account 2 webhooks
        primaryWebhook2 = WebhookConnectionEntity.builder()
                .connectionType("PRIMARY")
                .url("https://webhook2.example.com/primary")
                .accountId(200L)
                .active(true)
                .build();

        // Account 3 webhooks
        primaryWebhook3 = WebhookConnectionEntity.builder()
                .connectionType("PRIMARY")
                .url("https://webhook3.example.com/primary")
                .accountId(300L)
                .active(false)
                .build();

        // Persist all entities
        entityManager.persistAndFlush(primaryWebhook1);
        entityManager.persistAndFlush(fallbackWebhook1);
        entityManager.persistAndFlush(inactiveWebhook1);
        entityManager.persistAndFlush(primaryWebhook2);
        entityManager.persistAndFlush(primaryWebhook3);
    }

    @Test
    @DisplayName("should find webhooks by account ID")
    void shouldFindByAccountId() {
        List<WebhookConnectionEntity> account1Webhooks = webhookConnectionsJpaRepository.findByAccountId(100L);

        assertThat(account1Webhooks).hasSize(3);
        assertThat(account1Webhooks)
                .extracting(WebhookConnectionEntity::getAccountId)
                .containsOnly(100L);
        assertThat(account1Webhooks)
                .extracting(WebhookConnectionEntity::getConnectionType)
                .containsExactlyInAnyOrder("PRIMARY", "FALLBACK", "SECONDARY");
    }

    @Test
    @DisplayName("should find webhooks for different accounts separately")
    void shouldFindWebhooksForDifferentAccountsSeparately() {
        List<WebhookConnectionEntity> account2Webhooks = webhookConnectionsJpaRepository.findByAccountId(200L);
        List<WebhookConnectionEntity> account3Webhooks = webhookConnectionsJpaRepository.findByAccountId(300L);

        assertThat(account2Webhooks).hasSize(1);
        assertThat(account2Webhooks.get(0).getConnectionType()).isEqualTo("PRIMARY");
        assertThat(account2Webhooks.get(0).isActive()).isTrue();

        assertThat(account3Webhooks).hasSize(1);
        assertThat(account3Webhooks.get(0).getConnectionType()).isEqualTo("PRIMARY");
        assertThat(account3Webhooks.get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("should return empty list for non-existing account")
    void shouldReturnEmptyListForNonExistingAccount() {
        List<WebhookConnectionEntity> nonExistingAccountWebhooks = webhookConnectionsJpaRepository
                .findByAccountId(999L);

        assertThat(nonExistingAccountWebhooks).isEmpty();
    }

    @Test
    @DisplayName("should activate connections by IDs")
    void shouldActivateConnectionsByIds() {
        // Initially verify states
        assertThat(fallbackWebhook1.isActive()).isFalse();
        assertThat(inactiveWebhook1.isActive()).isFalse();

        // Activate specific connections
        Set<Long> activeIds = Set.of(fallbackWebhook1.getId(), inactiveWebhook1.getId());
        webhookConnectionsJpaRepository.activateConnections(activeIds);

        // Flush to apply changes and refresh entities
        entityManager.flush();
        entityManager.refresh(fallbackWebhook1);
        entityManager.refresh(inactiveWebhook1);
        entityManager.refresh(primaryWebhook1); // This should remain active

        // Verify activation
        assertThat(fallbackWebhook1.isActive()).isTrue();
        assertThat(inactiveWebhook1.isActive()).isTrue();
        assertThat(primaryWebhook1.isActive()).isTrue(); // Should remain unchanged
    }

    @Test
    @DisplayName("should deactivate connections not in active IDs")
    void shouldDeactivateConnectionsNotInActiveIds() {
        // Initially verify states
        assertThat(primaryWebhook1.isActive()).isTrue();
        assertThat(primaryWebhook2.isActive()).isTrue();

        // Keep only primaryWebhook1 active, deactivate others
        Set<Long> activeIds = Set.of(primaryWebhook1.getId());
        webhookConnectionsJpaRepository.deactivateConnections(activeIds);

        // Flush to apply changes and refresh entities
        entityManager.flush();
        entityManager.refresh(primaryWebhook1);
        entityManager.refresh(primaryWebhook2);
        entityManager.refresh(fallbackWebhook1);

        // Verify deactivation
        assertThat(primaryWebhook1.isActive()).isTrue(); // Should remain active (in activeIds)
        assertThat(primaryWebhook2.isActive()).isFalse(); // Should be deactivated (not in activeIds)
        assertThat(fallbackWebhook1.isActive()).isFalse(); // Should remain inactive
    }

    @Test
    @DisplayName("should handle empty active IDs for activation")
    void shouldHandleEmptyActiveIdsForActivation() {
        // Try to activate with empty set
        Set<Long> emptyActiveIds = Set.of();
        webhookConnectionsJpaRepository.activateConnections(emptyActiveIds);

        // Flush and refresh
        entityManager.flush();
        entityManager.refresh(primaryWebhook1);
        entityManager.refresh(fallbackWebhook1);

        // States should remain unchanged
        assertThat(primaryWebhook1.isActive()).isTrue();
        assertThat(fallbackWebhook1.isActive()).isFalse();
    }

    @Test
    @DisplayName("should handle empty active IDs for deactivation")
    void shouldHandleEmptyActiveIdsForDeactivation() {
        // Deactivate all (empty activeIds means nothing should remain active)
        Set<Long> emptyActiveIds = Set.of();
        webhookConnectionsJpaRepository.deactivateConnections(emptyActiveIds);

        // Flush and refresh
        entityManager.flush();
        entityManager.refresh(primaryWebhook1);
        entityManager.refresh(primaryWebhook2);

        // All should be deactivated
        assertThat(primaryWebhook1.isActive()).isFalse();
        assertThat(primaryWebhook2.isActive()).isFalse();
    }

    @Test
    @DisplayName("should handle activation and deactivation together")
    void shouldHandleActivationAndDeactivationTogether() {
        // Initially: primaryWebhook1=active, fallbackWebhook1=inactive, primaryWebhook2=active

        // Step 1: Activate fallbackWebhook1
        webhookConnectionsJpaRepository.activateConnections(Set.of(fallbackWebhook1.getId()));

        // Step 2: Keep only fallbackWebhook1 active, deactivate others
        webhookConnectionsJpaRepository.deactivateConnections(Set.of(fallbackWebhook1.getId()));

        // Flush and refresh all
        entityManager.flush();
        entityManager.refresh(primaryWebhook1);
        entityManager.refresh(fallbackWebhook1);
        entityManager.refresh(primaryWebhook2);

        // Verify final states
        assertThat(primaryWebhook1.isActive()).isFalse(); // Deactivated
        assertThat(fallbackWebhook1.isActive()).isTrue(); // Activated and kept active
        assertThat(primaryWebhook2.isActive()).isFalse(); // Deactivated
    }

    @Test
    @DisplayName("should handle non-existing IDs in activation")
    void shouldHandleNonExistingIdsInActivation() {
        Long nonExistingId = 999999L;
        Set<Long> idsWithNonExisting = Set.of(fallbackWebhook1.getId(), nonExistingId);

        // Should not throw exception and should activate existing IDs
        webhookConnectionsJpaRepository.activateConnections(idsWithNonExisting);

        entityManager.flush();
        entityManager.refresh(fallbackWebhook1);

        assertThat(fallbackWebhook1.isActive()).isTrue();
    }

    @Test
    @DisplayName("should handle non-existing IDs in deactivation")
    void shouldHandleNonExistingIdsInDeactivation() {
        Long nonExistingId = 999999L;
        Set<Long> idsWithNonExisting = Set.of(primaryWebhook1.getId(), nonExistingId);

        // Should not throw exception and should keep existing IDs active
        webhookConnectionsJpaRepository.deactivateConnections(idsWithNonExisting);

        entityManager.flush();
        entityManager.refresh(primaryWebhook1);
        entityManager.refresh(primaryWebhook2);

        assertThat(primaryWebhook1.isActive()).isTrue(); // Should remain active
        assertThat(primaryWebhook2.isActive()).isFalse(); // Should be deactivated
    }

    @Test
    @DisplayName("should verify basic CRUD operations work correctly")
    void shouldVerifyBasicCrudOperationsWorkCorrectly() {
        // Test findAll
        List<WebhookConnectionEntity> allWebhooks = webhookConnectionsJpaRepository.findAll();
        assertThat(allWebhooks).hasSize(5);

        // Test findById
        var foundWebhook = webhookConnectionsJpaRepository.findById(primaryWebhook1.getId());
        assertThat(foundWebhook).isPresent();
        assertThat(foundWebhook.get().getUrl()).isEqualTo("https://webhook1.example.com/primary");
        assertThat(foundWebhook.get().getConnectionType()).isEqualTo("PRIMARY");

        // Test save
        WebhookConnectionEntity newWebhook = WebhookConnectionEntity.builder()
                .connectionType("BACKUP")
                .url("https://backup.webhook.com")
                .accountId(400L)
                .active(false)
                .build();

        WebhookConnectionEntity savedWebhook = webhookConnectionsJpaRepository.save(newWebhook);
        assertThat(savedWebhook.getId()).isNotNull();
        assertThat(savedWebhook.getConnectionType()).isEqualTo("BACKUP");
        assertThat(savedWebhook.getAccountId()).isEqualTo(400L);

        // Test update
        savedWebhook.setActive(true);
        savedWebhook.setUrl("https://updated.webhook.com");
        WebhookConnectionEntity updatedWebhook = webhookConnectionsJpaRepository.save(savedWebhook);
        assertThat(updatedWebhook.isActive()).isTrue();
        assertThat(updatedWebhook.getUrl()).isEqualTo("https://updated.webhook.com");

        // Test delete
        webhookConnectionsJpaRepository.delete(savedWebhook);
        var deletedWebhook = webhookConnectionsJpaRepository.findById(savedWebhook.getId());
        assertThat(deletedWebhook).isEmpty();
    }

    @Test
    @DisplayName("should handle webhooks with different connection types")
    void shouldHandleWebhooksWithDifferentConnectionTypes() {
        List<WebhookConnectionEntity> allWebhooks = webhookConnectionsJpaRepository.findAll();

        List<String> connectionTypes = allWebhooks.stream()
                .map(WebhookConnectionEntity::getConnectionType)
                .distinct()
                .toList();

        assertThat(connectionTypes).containsExactlyInAnyOrder("PRIMARY", "FALLBACK", "SECONDARY");
    }

    @Test
    @DisplayName("should handle active and inactive webhooks correctly")
    void shouldHandleActiveAndInactiveWebhooksCorrectly() {
        List<WebhookConnectionEntity> allWebhooks = webhookConnectionsJpaRepository.findAll();

        List<WebhookConnectionEntity> activeWebhooks = allWebhooks.stream()
                .filter(WebhookConnectionEntity::isActive)
                .toList();

        List<WebhookConnectionEntity> inactiveWebhooks = allWebhooks.stream()
                .filter(webhook -> !webhook.isActive())
                .toList();

        assertThat(activeWebhooks).hasSize(2); // primaryWebhook1, primaryWebhook2
        assertThat(inactiveWebhooks).hasSize(3); // fallbackWebhook1, inactiveWebhook1, primaryWebhook3

        assertThat(activeWebhooks)
                .extracting(WebhookConnectionEntity::getConnectionType)
                .containsExactlyInAnyOrder("PRIMARY", "PRIMARY");
    }
}
