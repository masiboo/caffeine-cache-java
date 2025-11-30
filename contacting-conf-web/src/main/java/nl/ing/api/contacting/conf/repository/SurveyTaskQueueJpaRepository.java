package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyTaskQMappingWithName;
import nl.ing.api.contacting.java.repository.jpa.core.TaskQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyTaskQueueJpaRepository extends JpaRepository<TaskQueueEntity, Long> {

    @Query("""
            SELECT new nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyTaskQMappingWithName(m, t.friendlyName)
            FROM SurveyTaskQueueMappingEntity m
            JOIN TaskQueueEntity t ON m.taskQueueId = t.id
            WHERE m.surveyId = :surveyId
            """)
    List<SurveyTaskQMappingWithName> findTaskQueuesBySurveyId(@Param("surveyId") Long surveyId);
}
