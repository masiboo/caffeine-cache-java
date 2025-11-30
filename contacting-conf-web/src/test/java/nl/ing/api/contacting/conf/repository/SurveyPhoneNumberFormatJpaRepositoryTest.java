package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SurveyPhoneNumberFormatEntity;
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
class SurveyPhoneNumberFormatJpaRepositoryTest {

    @Autowired
    private SurveyPhoneNumberFormatJpaRepository surveyPhoneNumberFormatJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private SurveyPhoneNumberFormatEntity buildSurveyPhoneNumberFormat(Long surveyId, String format) {
        return SurveyPhoneNumberFormatEntity.builder()
            .id(null)
            .surveyId(surveyId)
            .format(format)
            .build();
    }

    @Test
    @DisplayName("deleteByIds removes entities with matching ids")
    void deleteByIds_removesEntitiesWithMatchingIds() {
        SurveyPhoneNumberFormatEntity entity1 = buildSurveyPhoneNumberFormat(1L, "E.164");
        SurveyPhoneNumberFormatEntity entity2 = buildSurveyPhoneNumberFormat(1L, "NATIONAL");
        SurveyPhoneNumberFormatEntity entity3 = buildSurveyPhoneNumberFormat(2L, "INTERNATIONAL");

        entityManager.persist(entity1);
        entityManager.persist(entity2);
        entityManager.persist(entity3);
        entityManager.flush();

        List<Long> idsToDelete = List.of(entity1.getId(), entity2.getId());
        surveyPhoneNumberFormatJpaRepository.deleteByIds(idsToDelete);
        entityManager.flush();

        List<SurveyPhoneNumberFormatEntity> remaining = surveyPhoneNumberFormatJpaRepository.findAll();
        assertThat(remaining)
            .hasSize(1)
            .allMatch(e -> e.getSurveyId().equals(2L));
    }

    @Test
    @DisplayName("deleteByIds does nothing if ids do not match")
    void deleteByIds_doesNothingIfIdsDoNotMatch() {
        SurveyPhoneNumberFormatEntity entity = buildSurveyPhoneNumberFormat(3L, "E.164");
        entityManager.persist(entity);
        entityManager.flush();

        surveyPhoneNumberFormatJpaRepository.deleteByIds(List.of(999L));
        entityManager.flush();

        List<SurveyPhoneNumberFormatEntity> remaining = surveyPhoneNumberFormatJpaRepository.findAll();
        assertThat(remaining).hasSize(1);
    }

    @Test
    @DisplayName("deleteByIds with empty list does nothing")
    void deleteByIds_withEmptyListDoesNothing() {
        SurveyPhoneNumberFormatEntity entity = buildSurveyPhoneNumberFormat(4L, "NATIONAL");
        entityManager.persist(entity);
        entityManager.flush();

        surveyPhoneNumberFormatJpaRepository.deleteByIds(List.of());
        entityManager.flush();

        List<SurveyPhoneNumberFormatEntity> remaining = surveyPhoneNumberFormatJpaRepository.findAll();
        assertThat(remaining).hasSize(1);
    }

    @Test
    @DisplayName("findBySurveyId returns all formats for given surveyId")
    void findBySurveyId_returnsAllFormatsForGivenSurveyId() {
        SurveyPhoneNumberFormatEntity entity1 = buildSurveyPhoneNumberFormat(5L, "E.164");
        SurveyPhoneNumberFormatEntity entity2 = buildSurveyPhoneNumberFormat(5L, "NATIONAL");
        SurveyPhoneNumberFormatEntity entity3 = buildSurveyPhoneNumberFormat(6L, "INTERNATIONAL");

        entityManager.persist(entity1);
        entityManager.persist(entity2);
        entityManager.persist(entity3);
        entityManager.flush();

        List<SurveyPhoneNumberFormatEntity> result = surveyPhoneNumberFormatJpaRepository.findBySurveyId(5L);
        assertThat(result)
            .hasSize(2)
            .extracting(SurveyPhoneNumberFormatEntity::getFormat)
            .containsExactlyInAnyOrder("E.164", "NATIONAL");
    }

    @Test
    @DisplayName("findBySurveyId returns empty list if no formats found")
    void findBySurveyId_returnsEmptyListIfNoFormatsFound() {
        List<SurveyPhoneNumberFormatEntity> result = surveyPhoneNumberFormatJpaRepository.findBySurveyId(999L);
        assertThat(result).isEmpty();
    }
}
