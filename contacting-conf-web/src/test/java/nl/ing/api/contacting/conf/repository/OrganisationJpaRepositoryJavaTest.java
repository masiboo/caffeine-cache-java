package nl.ing.api.contacting.conf.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationLevelEnumeration;
import nl.ing.api.contacting.java.repository.organisation.OrganisationJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("OrganisationJpaRepository Integration Tests")
class OrganisationJpaRepositoryJavaTest {

    @Autowired
    private OrganisationJpaRepository organisationJpaRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("should save and retrieve by parentId and accountId")
    void shouldSaveAndRetrieveByParentIdAndAccountId() {
        OrganisationEntity parent = OrganisationEntity.builder()
                .name("Parent Org")
                .accountId(1L).orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE)
                .build();

        organisationJpaRepository.save(parent);
        OrganisationEntity child = OrganisationEntity.builder()
                .name("Child Org")
                .parentId(parent.getId())
                .accountId(1L).orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();
        organisationJpaRepository.save(child);

        List<OrganisationEntity> found = organisationJpaRepository.findByParentIdAndAccountId(parent.getId(), 1L);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Child Org");
    }

    @Test
    @DisplayName("should find by ids and accountId")
    void shouldFindByIdsAndAccountId() {
        OrganisationEntity org1 = OrganisationEntity.builder().name("Org1").orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE).accountId(2L).build();
        OrganisationEntity org2 = OrganisationEntity.builder().name("Org2").accountId(2L).orgLevel(OrganisationLevelEnumeration.CLT).build();
        organisationJpaRepository.saveAll(List.of(org1, org2));
        Set<Long> ids = Set.of(org1.getId(), org2.getId());
        List<OrganisationEntity> found = organisationJpaRepository.findByIdsAndAccountId(ids, 2L);
        assertThat(found).hasSize(2);
    }

    @Test
    @DisplayName("should check existence by name, parentId, and accountId")
    void shouldCheckExistenceByNameParentIdAndAccountId() {
        OrganisationEntity parent = OrganisationEntity.builder().name("Parent").accountId(3L).orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE).build();
        organisationJpaRepository.save(parent);
        OrganisationEntity child = OrganisationEntity.builder().name("Child").parentId(parent.getId()).orgLevel(OrganisationLevelEnumeration.CIRCLE).accountId(3L).build();
        organisationJpaRepository.save(child);
        boolean exists = organisationJpaRepository.existsByNameAndParentIdAndAccountId("Child", parent.getId(), 3L);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should count children by parentId and accountId")
    void shouldCountChildrenByParentIdAndAccountId() {
        OrganisationEntity parent = OrganisationEntity.builder()
                .name("Parent")
                .accountId(4L)
                .orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE)
                .build();
        organisationJpaRepository.save(parent);
        OrganisationEntity child1 = OrganisationEntity.builder()
                .name("Child1")
                .parentId(parent.getId())
                .accountId(4L)
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();
        OrganisationEntity child2 = OrganisationEntity.builder()
                .name("Child2")
                .parentId(parent.getId())
                .accountId(4L)
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();
        organisationJpaRepository.saveAll(List.of(child1, child2));
        long count = organisationJpaRepository.countChildrenByParentIdAndAccountId(parent.getId(), 4L);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("should find all by accountId")
    void shouldFindAllByAccountId() {
        OrganisationEntity org1 = OrganisationEntity.builder().name("Org1").accountId(5L).orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE).build();
        OrganisationEntity org2 = OrganisationEntity.builder().name("Org2").orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE).accountId(5L).build();
        organisationJpaRepository.saveAll(List.of(org1, org2));
        List<OrganisationEntity[]> found = organisationJpaRepository.findAllByAccountId(5L);
        assertThat(found).isNotEmpty();
        assertThat(found).hasSize(2);
        assertThat(found.get(0)[0].getName()).isEqualTo("Org1");
        assertThat(found.get(1)[0].getName()).isEqualTo("Org2");
        assertThat(found.get(0)[0].getAccountId()).isEqualTo(5L);
        assertThat(found.get(1)[0].getAccountId()).isEqualTo(5L);
        assertThat(found.get(0)[0].getOrgLevel()).isEqualTo(OrganisationLevelEnumeration.SUPER_CIRCLE);
        assertThat(found.get(1)[0].getOrgLevel()).isEqualTo(OrganisationLevelEnumeration.SUPER_CIRCLE);
    }

    @Test
    @DisplayName("should find by name and accountId")
    void shouldFindByNameAndAccountId() {
        OrganisationEntity org = OrganisationEntity.builder().name("UniqueOrg").accountId(6L).orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE).build();
        organisationJpaRepository.save(org);
        List<OrganisationEntity> found = organisationJpaRepository.findByNameAndAccountId("UniqueOrg", 6L);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("UniqueOrg");
    }

    @Test
    @DisplayName("should find organisation tree using findByIdAndAccountId")
    void shouldFindOrganisationTreeUsingFindByIdAndAccountId() {
        // Arrange: create a 3-level hierarchy
        OrganisationEntity superCircle = OrganisationEntity.builder()
                .name("TreeOrg")
                .accountId(7L)
                .orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE)
                .build();
        organisationJpaRepository.save(superCircle);

        OrganisationEntity circle = OrganisationEntity.builder()
                .name("TreeOrg1")
                .accountId(7L)
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .parentId(superCircle.getId())
                .build();
        organisationJpaRepository.save(circle);

        OrganisationEntity clt = OrganisationEntity.builder()
                .name("TreeOrg2")
                .accountId(7L)
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .parentId(circle.getId())
                .build();
        organisationJpaRepository.save(clt);

        // Act: fetch the tree using the entity array method
        List<OrganisationEntity[]> result = organisationJpaRepository.findByIdAndAccountId(superCircle.getId(), 7L);

        // Use streams to evaluate the presence and correctness of each node in any position
        boolean foundSuperCircle = result.stream().flatMap(arr -> java.util.Arrays.stream(arr))
                .anyMatch(e -> e != null && "TreeOrg".equals(e.getName()) && e.getParentId() == null && e.getOrgLevel() == OrganisationLevelEnumeration.SUPER_CIRCLE);
        boolean foundCircle = result.stream().flatMap(arr -> java.util.Arrays.stream(arr))
                .anyMatch(e -> e != null && "TreeOrg1".equals(e.getName()) && e.getParentId() != null && e.getOrgLevel() == OrganisationLevelEnumeration.CIRCLE);
        boolean foundClt = result.stream().flatMap(arr -> java.util.Arrays.stream(arr))
                .anyMatch(e -> e != null && "TreeOrg2".equals(e.getName()) && e.getParentId() != null && e.getOrgLevel() == OrganisationLevelEnumeration.CLT);

        assertThat(foundSuperCircle)
                .as("Expected to find superCircle node 'TreeOrg' in result. See logs for actual result rows.")
                .isTrue();
        assertThat(foundCircle)
                .as("Expected to find circle node 'TreeOrg1' in result. See logs for actual result rows.")
                .isTrue();
        assertThat(foundClt)
                .as("Expected to find clt node 'TreeOrg2' in result. See logs for actual result rows.")
                .isTrue();
    }

    @Test
    @DisplayName("should find by id and accountId")
    void shouldFindByIdAndAccountId() {
        OrganisationEntity org = OrganisationEntity.builder().name("FindByIdOrg").accountId(8L).orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE).build();
        organisationJpaRepository.save(org);
        List<OrganisationEntity[]> found = organisationJpaRepository.findByIdAndAccountId(org.getId(), 8L);
        assertThat(found).isNotEmpty();
        assertThat(found).hasSize(1);
        assertThat(found.get(0)[0].getName()).isEqualTo("FindByIdOrg");
        assertThat(found.get(0)[0].getAccountId()).isEqualTo(8L);
        assertThat(found.get(0)[0].getOrgLevel()).isEqualTo(OrganisationLevelEnumeration.SUPER_CIRCLE);
    }

    @Test
    @DisplayName("should delete organisation entity by id")
    void shouldDeleteOrganisationEntityById() {
        OrganisationEntity org = OrganisationEntity.builder()
                .name("ToDelete")
                .accountId(100L)
                .orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE)
                .build();
        organisationJpaRepository.save(org);

        Long id = org.getId();
        assertThat(organisationJpaRepository.findById(id)).isPresent();

        organisationJpaRepository.deleteById(id);

        assertThat(organisationJpaRepository.findById(id)).isNotPresent();
    }

    @Test
    @DisplayName("should update organisation entity")
    void shouldUpdateOrganisationEntity() {
        OrganisationEntity org = OrganisationEntity.builder()
                .name("ToUpdate")
                .accountId(200L)
                .orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE)
                .build();
        organisationJpaRepository.save(org);

        Long id = org.getId();
        OrganisationEntity saved = organisationJpaRepository.findById(id).orElseThrow();
        saved.setName("UpdatedName");
        organisationJpaRepository.save(saved);

        OrganisationEntity updated = organisationJpaRepository.findById(id).orElseThrow();
        assertThat(updated.getName()).isEqualTo("UpdatedName");
    }

    @Test
    @DisplayName("should fail to update organisation entity if id not found")
    void shouldFailToUpdateOrganisationEntityIfIdNotFound() {
        Long nonExistentId = -999L;
        // Try to find a non-existent entity and expect NoSuchElementException
        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() -> {
            OrganisationEntity entity = organisationJpaRepository.findById(nonExistentId).orElseThrow();
            entity.setName("ShouldNotUpdate");
            organisationJpaRepository.save(entity);
        });
        assertThat(thrown)
                .as("Updating a non-existent organisation entity should throw NoSuchElementException")
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    @DisplayName("should not delete organisation entity with children using deleteByIdAndAccountId")
    void shouldNotDeleteOrganisationEntityWithChildrenUsingDeleteByIdAndAccountId() {
        // Arrange: create parent and child
        OrganisationEntity parent = OrganisationEntity.builder()
                .name("ParentWithChild")
                .accountId(300L)
                .orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE)
                .build();
        organisationJpaRepository.save(parent);
        OrganisationEntity child = OrganisationEntity.builder()
                .name("ChildOfParent")
                .parentId(parent.getId())
                .accountId(300L)
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();
        organisationJpaRepository.save(child);

        Long parentId = parent.getId();
        Long childId = child.getId();
        Long accountId = parent.getAccountId();

        // Act & Assert: try to delete parent using deleteByIdAndAccountId, expect DataIntegrityViolationException
        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
                organisationJpaRepository.deleteByIdAndAccountId(parentId, accountId)
        );

        assertThat(thrown)
                .as("Should throw DataIntegrityViolationException when trying to delete parent with children")
                .isInstanceOf(DataIntegrityViolationException.class);

        // Parent and child should still exist
        assertThat(organisationJpaRepository.findById(parentId)).isPresent();
        assertThat(organisationJpaRepository.findById(childId)).isPresent();
    }

    @Test
    @DisplayName("should find organisation tree using findOrgTree")
    void shouldFindOrganisationTreeUsingFindOrgTree() {
        // Arrange: create a 3-level hierarchy
        OrganisationEntity superCircle = OrganisationEntity.builder().name("SuperCircle").accountId(101L).orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE).build();
        organisationJpaRepository.save(superCircle);

        OrganisationEntity circle = OrganisationEntity.builder().name("Circle").accountId(101L).orgLevel(OrganisationLevelEnumeration.CIRCLE).parentId(superCircle.getId()).build();
        organisationJpaRepository.save(circle);

        OrganisationEntity team = OrganisationEntity.builder().name("Team").accountId(101L).orgLevel(OrganisationLevelEnumeration.CLT).parentId(circle.getId()).build();
        organisationJpaRepository.save(team);

        OrganisationEntity orphanteam = OrganisationEntity.builder().name("orphanteam").accountId(101L).orgLevel(OrganisationLevelEnumeration.CLT).build();
        organisationJpaRepository.save(team);
        List<OrganisationEntity[]> result = organisationJpaRepository.findOrgTree(101L, OrganisationLevelEnumeration.SUPER_CIRCLE);
        // Assert
        assertThat(result).isNotEmpty();
        boolean found = result.stream().anyMatch(arr -> arr[0] != null && "SuperCircle".equals(arr[0].getName()) && arr[1] != null && "Circle".equals(arr[1].getName()) && arr[2] != null && "Team".equals(arr[2].getName()));
        assertThat(found).as("Expected to find the full organisation tree structure").isTrue();
    }
}
