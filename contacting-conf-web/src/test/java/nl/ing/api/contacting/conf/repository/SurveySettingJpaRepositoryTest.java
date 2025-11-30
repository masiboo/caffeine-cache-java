package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SurveySettingsEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SurveySettingJpaRepositoryTest {

    @Autowired
    private SurveySettingJpaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private SurveySettingsEntity buildSurveySettings(Long accountId, String name) {
        return SurveySettingsEntity.builder()
                .id(null)
                .name(name)
                .channel("EMAIL")
                .channelDirection("OUTBOUND")
                .voiceSurveyId("voiceSurvey123")
                .surveyForTransfers(true)
                .accountId(accountId)
                .build();
    }

    @Nested
    @DisplayName("findByAccountId")
    class FindByAccountId {

        @Test
        @DisplayName("should return surveys for given accountId")
        void shouldReturnSurveysForGivenAccountId() {
            SurveySettingsEntity entity = buildSurveySettings(1L, "Survey1");
            entityManager.persistAndFlush(entity);

            List<SurveySettingsEntity> result = repository.findByAccountId(1L);

            assertEquals(1, result.size());
            assertEquals("Survey1", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no surveys for accountId")
        void shouldReturnEmptyListWhenNoSurveysForAccountId() {
            List<SurveySettingsEntity> result = repository.findByAccountId(999L);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByIdAndAccountId")
    class FindByIdAndAccountId {

        @Test
        @DisplayName("should return survey for given id and accountId")
        void shouldReturnSurveyForGivenIdAndAccountId() {
            SurveySettingsEntity entity = buildSurveySettings(2L, "Survey2");
            SurveySettingsEntity saved = entityManager.persistAndFlush(entity);

            Optional<SurveySettingsEntity> result = repository.findByIdAndAccountId(saved.getId(), 2L);

            assertTrue(result.isPresent());
            assertEquals("Survey2", result.get().getName());
        }

        @Test
        @DisplayName("should return empty when survey not found for id and accountId")
        void shouldReturnEmptyWhenSurveyNotFoundForIdAndAccountId() {
            Optional<SurveySettingsEntity> result = repository.findByIdAndAccountId(999L, 999L);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteByIdAndAccountId")
    class DeleteByIdAndAccountId {

        @Test
        @DisplayName("should delete survey for given id and accountId")
        void shouldDeleteSurveyForGivenIdAndAccountId() {
            SurveySettingsEntity entity = buildSurveySettings(3L, "Survey3");
            SurveySettingsEntity saved = entityManager.persistAndFlush(entity);

            int deleted = repository.deleteByIdAndAccountId(saved.getId(), 3L);
            entityManager.flush();
            entityManager.clear();

            assertEquals(1, deleted);
            assertTrue(repository.findById(saved.getId()).isEmpty());
        }

        @Test
        @DisplayName("should not delete when id and accountId do not match")
        void shouldNotDeleteWhenIdAndAccountIdDoNotMatch() {
            SurveySettingsEntity entity = buildSurveySettings(4L, "Survey4");
            SurveySettingsEntity saved = entityManager.persistAndFlush(entity);

            int deleted = repository.deleteByIdAndAccountId(saved.getId(), 999L);
            entityManager.flush();

            assertEquals(0, deleted);
            assertTrue(repository.findById(saved.getId()).isPresent());
        }
    }

    @Nested
    @DisplayName("JpaRepository methods")
    class JpaRepositoryMethods {

        @Test
        @DisplayName("should save survey settings")
        void shouldSaveSurveySettings() {
            SurveySettingsEntity entity = buildSurveySettings(5L, "Survey5");
            SurveySettingsEntity saved = repository.save(entity);

            assertNotNull(saved.getId());
            assertEquals("Survey5", saved.getName());
        }

        @Test
        @DisplayName("should find survey settings by id")
        void shouldFindSurveySettingsById() {
            SurveySettingsEntity entity = buildSurveySettings(6L, "Survey6");
            SurveySettingsEntity saved = entityManager.persistAndFlush(entity);

            Optional<SurveySettingsEntity> found = repository.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals("Survey6", found.get().getName());
        }

        @Test
        @DisplayName("should return empty when survey settings not found by id")
        void shouldReturnEmptyWhenSurveySettingsNotFoundById() {
            Optional<SurveySettingsEntity> found = repository.findById(999L);
            assertTrue(found.isEmpty());
        }
    }
}
