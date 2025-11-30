package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SurveyTaskQueueMappingEntity;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyTaskQMappingWithName;
import nl.ing.api.contacting.java.repository.jpa.core.TaskQueueEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackages = {"nl.ing.api.contacting.conf.domain.entity","nl.ing.api.contacting.java.repository.jpa.core"})
class SurveyTaskQueueJpaRepositoryTest extends BaseJpaTest {

    @Autowired
    private SurveyTaskQueueJpaRepository surveyTaskQueueJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TaskQueueEntity taskQueueEntity;
    private TaskQueueEntity taskQueueEntity1;

    @BeforeEach
    void setUp() {
        taskQueueEntity = new TaskQueueEntity(
                null,
                "reservation123",
                "assignment456",
                "workerA,workerB",
                "Support Queue",
                "sid789",
                LocalDateTime.now(),
                true,
                1L,
                "system",
                ZonedDateTime.now()
        );

        taskQueueEntity1 = new TaskQueueEntity(
                null,
                "reservation123",
                "assignment456",
                "workerA,workerB",
                "Sales Queue",
                "sid789",
                LocalDateTime.now(),
                true,
                1L,
                "system",
                ZonedDateTime.now()
        );

        entityManager.persistAndFlush(taskQueueEntity);
        entityManager.persistAndFlush(taskQueueEntity1);

    }

    private SurveyTaskQueueMappingEntity buildSurveyTaskQueueMapping(Long surveyId, TaskQueueEntity taskQueueEntity) {
        return SurveyTaskQueueMappingEntity.builder()
                .surveyId(surveyId)
                .taskQueueId(taskQueueEntity.getId())
                .build();
    }

    @Test
    @DisplayName("findTaskQueuesBySurveyId returns mappings when surveyId exists")
    void findTaskQueuesBySurveyId_shouldReturnMappings_whenSurveyIdExists() {
        // Given
        Long surveyId = 1L;

        //entityManager.persistAndFlush(taskQueueEntity);

        SurveyTaskQueueMappingEntity mapping = buildSurveyTaskQueueMapping(surveyId, taskQueueEntity);

        entityManager.persistAndFlush(mapping);

        // When
        List<SurveyTaskQMappingWithName> result = surveyTaskQueueJpaRepository.findTaskQueuesBySurveyId(surveyId);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).friendlyName()).isEqualTo("Support Queue");
    }

    @Test
    @DisplayName("findTaskQueuesBySurveyId returns empty when surveyId does not exist")
    void findTaskQueuesBySurveyId_shouldReturnEmpty_whenSurveyIdNotExists() {
        // Given
        Long nonExistentSurveyId = 999L;

        // When
        List<SurveyTaskQMappingWithName> result = surveyTaskQueueJpaRepository.findTaskQueuesBySurveyId(nonExistentSurveyId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findTaskQueuesBySurveyId returns multiple mappings for same survey")
    void findTaskQueuesBySurveyId_shouldReturnMultipleMappings_whenMultipleQueuesExist() {
        // Given
        Long surveyId = 1L;

        SurveyTaskQueueMappingEntity supportMapping = buildSurveyTaskQueueMapping(surveyId, taskQueueEntity);
        SurveyTaskQueueMappingEntity salesMapping = buildSurveyTaskQueueMapping(surveyId, taskQueueEntity1);

        entityManager.persistAndFlush(supportMapping);
        entityManager.persistAndFlush(salesMapping);

        // When
        List<SurveyTaskQMappingWithName> result = surveyTaskQueueJpaRepository.findTaskQueuesBySurveyId(surveyId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(SurveyTaskQMappingWithName::friendlyName)
                .containsExactlyInAnyOrder("Support Queue", "Sales Queue");
    }
}
