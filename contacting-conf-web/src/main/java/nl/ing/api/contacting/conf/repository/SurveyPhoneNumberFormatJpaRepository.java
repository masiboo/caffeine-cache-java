package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SurveyPhoneNumberFormatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyPhoneNumberFormatJpaRepository extends JpaRepository<SurveyPhoneNumberFormatEntity, Long> {

    @Modifying
    @Query("DELETE FROM SurveyPhoneNumberFormatEntity e WHERE e.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);

    List<SurveyPhoneNumberFormatEntity> findBySurveyId(Long surveyId);

}
