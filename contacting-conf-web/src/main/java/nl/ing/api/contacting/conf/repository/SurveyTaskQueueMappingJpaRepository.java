package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SurveyTaskQueueMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyTaskQueueMappingJpaRepository extends JpaRepository<SurveyTaskQueueMappingEntity, Long> {

    @Modifying
    @Query("DELETE FROM SurveyTaskQueueMappingEntity e WHERE e.surveyId = :surveyId AND e.taskQueueId IN :taskQueueIds")
    void deleteBySurveyIdAndTaskQueueIds(@Param("surveyId") Long surveyId, @Param("taskQueueIds") List<Long> taskQueueIds);
}
