package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SurveyTaskQueueMappingEntity;
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
class SurveyTaskQueueMappingJpaRepositoryTest {

    @Autowired
    private SurveyTaskQueueMappingJpaRepository surveyTaskQueueMappingJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private SurveyTaskQueueMappingEntity buildSurveyTaskQueueMapping(Long surveyId, Long taskQueueId) {
        return SurveyTaskQueueMappingEntity.builder()
            .id(null)
            .surveyId(surveyId)
            .taskQueueId(taskQueueId)
            .build();
    }

    @Test
    @DisplayName("deleteBySurveyIdAndTaskQueueIds removes matching entities")
    void deleteBySurveyIdAndTaskQueueIds_removesMatchingEntities() {
        Long surveyId = 1L;
        SurveyTaskQueueMappingEntity mapping1 = buildSurveyTaskQueueMapping(surveyId, 10L);
        SurveyTaskQueueMappingEntity mapping2 = buildSurveyTaskQueueMapping(surveyId, 20L);
        SurveyTaskQueueMappingEntity mappingOther = buildSurveyTaskQueueMapping(2L, 10L);

        entityManager.persist(mapping1);
        entityManager.persist(mapping2);
        entityManager.persist(mappingOther);
        entityManager.flush();

        surveyTaskQueueMappingJpaRepository.deleteBySurveyIdAndTaskQueueIds(surveyId, List.of(10L, 20L));
        entityManager.flush();

        List<SurveyTaskQueueMappingEntity> remaining = surveyTaskQueueMappingJpaRepository.findAll();
        assertThat(remaining)
            .hasSize(1)
            .allMatch(e -> e.getSurveyId().equals(2L));
    }

    @Test
    @DisplayName("deleteBySurveyIdAndTaskQueueIds does nothing if no match")
    void deleteBySurveyIdAndTaskQueueIds_doesNothingIfNoMatch() {
        SurveyTaskQueueMappingEntity mapping = buildSurveyTaskQueueMapping(3L, 30L);
        entityManager.persist(mapping);
        entityManager.flush();

        surveyTaskQueueMappingJpaRepository.deleteBySurveyIdAndTaskQueueIds(99L, List.of(999L));
        entityManager.flush();

        List<SurveyTaskQueueMappingEntity> remaining = surveyTaskQueueMappingJpaRepository.findAll();
        assertThat(remaining).hasSize(1);
    }

    @Test
    @DisplayName("deleteBySurveyIdAndTaskQueueIds with empty list does nothing")
    void deleteBySurveyIdAndTaskQueueIds_withEmptyListDoesNothing() {
        SurveyTaskQueueMappingEntity mapping = buildSurveyTaskQueueMapping(4L, 40L);
        entityManager.persist(mapping);
        entityManager.flush();

        surveyTaskQueueMappingJpaRepository.deleteBySurveyIdAndTaskQueueIds(4L, List.of());
        entityManager.flush();

        List<SurveyTaskQueueMappingEntity> remaining = surveyTaskQueueMappingJpaRepository.findAll();
        assertThat(remaining).hasSize(1);
    }
}
