package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SurveySettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveySettingJpaRepository extends JpaRepository<SurveySettingsEntity, Long> {

    List<SurveySettingsEntity> findByAccountId(Long accountId);

    Optional<SurveySettingsEntity> findByIdAndAccountId(Long surveyId, Long accountId);

    @Modifying
    @Query("DELETE FROM SurveySettingsEntity e WHERE e.id = :surveyId AND e.accountId = :accountId")
    int deleteByIdAndAccountId(@Param("surveyId") Long surveyId, @Param("accountId") Long accountId);

}
