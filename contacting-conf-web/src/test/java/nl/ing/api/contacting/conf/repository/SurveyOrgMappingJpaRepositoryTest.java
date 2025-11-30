package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SurveyOrgMappingEntity;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyOrgDetails;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationLevelEnumeration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SurveyOrgMappingJpaRepositoryTest {

    @Autowired
    private SurveyOrgMappingJpaRepository surveyOrgMappingJpaRepository;

    @Autowired
    private TestEntityManager entityManager;


    private OrganisationEntity buildOrganisation(String name, OrganisationEntity parent) {
        return OrganisationEntity.builder()
                .name(name)
                .accountId(1L)
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .parent(parent)
                .build();

    }

    private SurveyOrgMappingEntity buildSurveyOrgMapping(Long surveyId, OrganisationEntity organisation) {
        return SurveyOrgMappingEntity.builder()
                .surveyId(surveyId)
                .orgId(organisation.getId())
                .organisation(organisation)
                .build();
    }

    @Test
    @DisplayName("findBySurveyIdWithHierarchy returns all details for given surveyId")
    void findBySurveyIdWithHierarchy_returnsAllDetailsForGivenSurveyId() {

        OrganisationEntity grandParent = buildOrganisation("GrandParent", null);
        OrganisationEntity persistedGrandParent = entityManager.persistAndFlush(grandParent);

        OrganisationEntity parent = buildOrganisation("Parent", persistedGrandParent);
        OrganisationEntity persistedParent = entityManager.persistAndFlush(parent);

        OrganisationEntity org = buildOrganisation("Org", persistedParent);
        OrganisationEntity persistedOrg = entityManager.persistAndFlush(org);

        SurveyOrgMappingEntity mapping = buildSurveyOrgMapping(500L, persistedOrg);
        entityManager.persistAndFlush(mapping);

        List<SurveyOrgDetails> result = surveyOrgMappingJpaRepository.findBySurveyIdWithHierarchy(500L);

        assertThat(result).hasSize(1);
        SurveyOrgDetails details = result.get(0);

        assertThat(details.org().getName()).isEqualTo("Org");

    }

    @Test
    @DisplayName("deleteBySurveyIdAndOrgIds removes matching entities")
    void deleteBySurveyIdAndOrgIds_removesMatchingEntities() {
        OrganisationEntity org1 = buildOrganisation("Org1", null);
        OrganisationEntity org2 = buildOrganisation("Org2", null);
        OrganisationEntity persistedOrg1 = entityManager.persistAndFlush(org1);
        OrganisationEntity persistedOrg2 = entityManager.persistAndFlush(org2);

        SurveyOrgMappingEntity mapping1 = buildSurveyOrgMapping(100L, persistedOrg1);
        SurveyOrgMappingEntity mapping2 = buildSurveyOrgMapping(100L, persistedOrg2);
        SurveyOrgMappingEntity mappingOther = buildSurveyOrgMapping(200L, persistedOrg1);

        entityManager.persist(mapping1);
        entityManager.persist(mapping2);
        entityManager.persist(mappingOther);
        entityManager.flush();

        surveyOrgMappingJpaRepository.deleteBySurveyIdAndOrgIds(100L, List.of(persistedOrg1.getId(), persistedOrg2.getId()));
        entityManager.flush();

        List<SurveyOrgMappingEntity> remaining = surveyOrgMappingJpaRepository.findAll();
        assertThat(remaining)
                .hasSize(1)
                .allMatch(e -> e.getSurveyId().equals(200L));
    }

    @Test
    @DisplayName("deleteBySurveyIdAndOrgIds does nothing if no match")
    void deleteBySurveyIdAndOrgIds_doesNothingIfNoMatch() {
        OrganisationEntity org = buildOrganisation("Org3", null);
        OrganisationEntity persistedOrg = entityManager.persistAndFlush(org);

        SurveyOrgMappingEntity mapping = buildSurveyOrgMapping(300L, persistedOrg);
        entityManager.persist(mapping);
        entityManager.flush();

        surveyOrgMappingJpaRepository.deleteBySurveyIdAndOrgIds(999L, List.of(999L));
        entityManager.flush();

        List<SurveyOrgMappingEntity> remaining = surveyOrgMappingJpaRepository.findAll();
        assertThat(remaining).hasSize(1);
    }

    @Test
    @DisplayName("deleteBySurveyIdAndOrgIds with empty list does nothing")
    void deleteBySurveyIdAndOrgIds_withEmptyListDoesNothing() {
        OrganisationEntity org = buildOrganisation("Org4", null);
        OrganisationEntity persistedOrg = entityManager.persistAndFlush(org);

        SurveyOrgMappingEntity mapping = buildSurveyOrgMapping(400L, persistedOrg);
        entityManager.persist(mapping);
        entityManager.flush();

        surveyOrgMappingJpaRepository.deleteBySurveyIdAndOrgIds(400L, List.of());
        entityManager.flush();

        List<SurveyOrgMappingEntity> remaining = surveyOrgMappingJpaRepository.findAll();
        assertThat(remaining).hasSize(1);
    }

    @Test
    @DisplayName("findBySurveyIdWithHierarchy returns empty list if no details found")
    void findBySurveyIdWithHierarchy_returnsEmptyListIfNoDetailsFound() {
        List<SurveyOrgDetails> result = surveyOrgMappingJpaRepository.findBySurveyIdWithHierarchy(999L);
        assertThat(result).isEmpty();
    }
}
