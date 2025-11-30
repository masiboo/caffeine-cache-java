package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.AttributeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AttributeJpaRepository Integration Tests")
class AttributeJpaRepositoryIntegrationTest {

    @Autowired
    private AttributeJpaRepository attributeJpaRepository;

    @Test
    @DisplayName("should find attributes by account id across multiple accounts")
    void shouldFindAttributesByAccountIdAcrossMultipleAccounts() {
        // Create test data for multiple accounts
        var account1Attribute1 = AttributeEntity.builder()
                .entity("PREFERENCE")
                .entityValue("theme")
                .label("Theme Setting")
                .labelValue("dark")
                .labelContent("Dark theme preference")
                .displayOrder(1)
                .accountId(100L)
                .build();

        var account1Attribute2 = AttributeEntity.builder()
                .entity("PREFERENCE")
                .entityValue("language")
                .label("Language Setting")
                .labelValue("en")
                .labelContent("English language preference")
                .displayOrder(2)
                .accountId(100L)
                .build();

        var account2Attribute1 = AttributeEntity.builder()
                .entity("PREFERENCE")
                .entityValue("theme")
                .label("Theme Setting")
                .labelValue("light")
                .labelContent("Light theme preference")
                .displayOrder(1)
                .accountId(200L)
                .build();

        attributeJpaRepository.saveAll(List.of(
                account1Attribute1,
                account1Attribute2,
                account2Attribute1
        ));

        // Test finding by account 1
        List<AttributeEntity> account1Attributes = attributeJpaRepository
                .findByAccountId(100L);

        assertThat(account1Attributes).hasSize(2);
        assertThat(account1Attributes)
                .extracting(AttributeEntity::getEntityValue)
                .containsExactlyInAnyOrder("theme", "language");

        // Test finding by account 2
        List<AttributeEntity> account2Attributes = attributeJpaRepository
                .findByAccountId(200L);

        assertThat(account2Attributes).hasSize(1);
        assertThat(account2Attributes.get(0).getEntityValue()).isEqualTo("theme");
        assertThat(account2Attributes.get(0).getLabelValue()).isEqualTo("light");

        // Test finding by non-existent account
        List<AttributeEntity> nonExistentAccountAttributes = attributeJpaRepository
                .findByAccountId(999L);

        assertThat(nonExistentAccountAttributes).isEmpty();
    }

    @Test
    @DisplayName("should find attribute by id and account id with proper isolation")
    void shouldFindAttributeByIdAndAccountIdWithProperIsolation() {
        var attribute1 = AttributeEntity.builder()
                .entity("SETTING")
                .entityValue("privacy")
                .label("Privacy Setting")
                .labelValue("public")
                .labelContent("Public privacy setting")
                .displayOrder(1)
                .accountId(100L)
                .build();

        var attribute2 = AttributeEntity.builder()
                .entity("SETTING")
                .entityValue("privacy")
                .label("Privacy Setting")
                .labelValue("private")
                .labelContent("Private privacy setting")
                .displayOrder(1)
                .accountId(200L)
                .build();

        AttributeEntity savedAttribute1 = attributeJpaRepository.save(attribute1);
        AttributeEntity savedAttribute2 = attributeJpaRepository.save(attribute2);

        // Test finding attribute with correct account
        Optional<AttributeEntity> foundAttribute1 = attributeJpaRepository
                .findByIdAndAccountId(savedAttribute1.getId(), 100L);

        assertThat(foundAttribute1).isPresent();
        assertThat(foundAttribute1.get().getLabelValue()).isEqualTo("public");
        assertThat(foundAttribute1.get().getAccountId()).isEqualTo(100L);

        // Test finding attribute with wrong account (should not find)
        Optional<AttributeEntity> notFoundAttribute = attributeJpaRepository
                .findByIdAndAccountId(savedAttribute1.getId(), 200L);

        assertThat(notFoundAttribute).isEmpty();

        // Test finding with non-existent id
        Optional<AttributeEntity> nonExistentAttribute = attributeJpaRepository
                .findByIdAndAccountId(999L, 100L);

        assertThat(nonExistentAttribute).isEmpty();
    }

    @Test
    @DisplayName("should maintain referential integrity and consistency")
    void shouldMaintainReferentialIntegrityAndConsistency() {
        var attribute = AttributeEntity.builder()
                .entity("TEST")
                .entityValue("integrity")
                .label("Integrity Test")
                .labelValue("test-value")
                .labelContent("Test content for integrity")
                .displayOrder(5)
                .accountId(400L)
                .build();

        AttributeEntity savedAttribute = attributeJpaRepository.save(attribute);
        assertThat(savedAttribute.getId()).isNotNull();

        // Find by different criteria should return the same entity
        Optional<AttributeEntity> foundById = attributeJpaRepository.findById(savedAttribute.getId());
        Optional<AttributeEntity> foundByIdAndAccount = attributeJpaRepository
                .findByIdAndAccountId(savedAttribute.getId(), 400L);
        List<AttributeEntity> foundByAccount = attributeJpaRepository.findByAccountId(400L);

        assertThat(foundById).isPresent();
        assertThat(foundByIdAndAccount).isPresent();
        assertThat(foundByAccount).hasSize(1);

        // Verify all methods return the same entity
        assertThat(foundById.get().getId()).isEqualTo(savedAttribute.getId());
        assertThat(foundByIdAndAccount.get().getId()).isEqualTo(savedAttribute.getId());
        assertThat(foundByAccount.get(0).getId()).isEqualTo(savedAttribute.getId());

        // Verify entity properties
        assertThat(foundById.get().getEntity()).isEqualTo("TEST");
        assertThat(foundById.get().getEntityValue()).isEqualTo("integrity");
        assertThat(foundById.get().getLabelValue()).isEqualTo("test-value");
        assertThat(foundById.get().getAccountId()).isEqualTo(400L);
    }

    @Test
    @DisplayName("should handle empty results gracefully")
    void shouldHandleEmptyResultsGracefully() {
        // Test finding by non-existent account
        List<AttributeEntity> emptyAccountResult = attributeJpaRepository
                .findByAccountId(999L);

        assertThat(emptyAccountResult).isEmpty();

        // Test finding by non-existent id and account combination
        Optional<AttributeEntity> emptyIdAccountResult = attributeJpaRepository
                .findByIdAndAccountId(999L, 999L);

        assertThat(emptyIdAccountResult).isEmpty();
    }

    @Test
    @DisplayName("should support CRUD operations correctly")
    void shouldSupportCrudOperationsCorrectly() {
        var originalAttribute = AttributeEntity.builder()
                .entity("NOTIFICATION")
                .entityValue("sound")
                .label("Sound Setting")
                .labelValue("enabled")
                .labelContent("Notification sound setting")
                .displayOrder(3)
                .accountId(500L)
                .build();

        // Create
        AttributeEntity savedAttribute = attributeJpaRepository.save(originalAttribute);
        assertThat(savedAttribute.getId()).isNotNull();

        // Read
        Optional<AttributeEntity> foundAttribute = attributeJpaRepository
                .findByIdAndAccountId(savedAttribute.getId(), 500L);
        assertThat(foundAttribute).isPresent();

        // Update
        AttributeEntity updatedAttribute = AttributeEntity.builder()
                .id(savedAttribute.getId())
                .entity(savedAttribute.getEntity())
                .entityValue(savedAttribute.getEntityValue())
                .label(savedAttribute.getLabel())
                .labelValue("disabled")
                .labelContent(savedAttribute.getLabelContent())
                .displayOrder(savedAttribute.getDisplayOrder())
                .accountId(savedAttribute.getAccountId())
                .build();

        attributeJpaRepository.save(updatedAttribute);

        Optional<AttributeEntity> reloadedAttribute = attributeJpaRepository
                .findByIdAndAccountId(savedAttribute.getId(), 500L);
        assertThat(reloadedAttribute).isPresent();
        assertThat(reloadedAttribute.get().getLabelValue()).isEqualTo("disabled");

        // Delete
        attributeJpaRepository.deleteById(savedAttribute.getId());

        Optional<AttributeEntity> deletedAttribute = attributeJpaRepository
                .findByIdAndAccountId(savedAttribute.getId(), 500L);
        assertThat(deletedAttribute).isEmpty();
    }

    @Test
    @DisplayName("should handle optional fields correctly")
    void shouldHandleOptionalFieldsCorrectly() {
        // Test with null optional fields
        var attributeWithNulls = AttributeEntity.builder()
                .entity("MINIMAL")
                .entityValue("test")
                .label("Minimal Test")
                .labelValue("value")
                .labelContent(null)
                .displayOrder(null)
                .accountId(600L)
                .build();

        AttributeEntity savedAttribute = attributeJpaRepository.save(attributeWithNulls);

        Optional<AttributeEntity> foundAttribute = attributeJpaRepository
                .findByIdAndAccountId(savedAttribute.getId(), 600L);

        assertThat(foundAttribute).isPresent();
        assertThat(foundAttribute.get().getLabelContentOptional()).isEmpty();
        assertThat(foundAttribute.get().getDisplayOrderOptional()).isEmpty();

        // Test with populated optional fields
        var attributeWithOptionals = AttributeEntity.builder()
                .entity("COMPLETE")
                .entityValue("test")
                .label("Complete Test")
                .labelValue("value")
                .labelContent("Optional content")
                .displayOrder(10)
                .accountId(600L)
                .build();

        AttributeEntity savedCompleteAttribute = attributeJpaRepository.save(attributeWithOptionals);

        Optional<AttributeEntity> foundCompleteAttribute = attributeJpaRepository
                .findByIdAndAccountId(savedCompleteAttribute.getId(), 600L);

        assertThat(foundCompleteAttribute).isPresent();
        assertThat(foundCompleteAttribute.get().getLabelContentOptional()).isPresent();
        assertThat(foundCompleteAttribute.get().getLabelContentOptional().get()).isEqualTo("Optional content");
        assertThat(foundCompleteAttribute.get().getDisplayOrderOptional()).isPresent();
        assertThat(foundCompleteAttribute.get().getDisplayOrderOptional().get()).isEqualTo(10);
    }
}