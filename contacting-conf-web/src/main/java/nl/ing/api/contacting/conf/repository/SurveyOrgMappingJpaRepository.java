package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SurveyOrgMappingEntity;
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyOrgDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyOrgMappingJpaRepository extends JpaRepository<SurveyOrgMappingEntity, Long> {

    @Modifying
    @Query("DELETE FROM SurveyOrgMappingEntity e WHERE e.surveyId = :surveyId AND e.orgId IN :orgIds")
    void deleteBySurveyIdAndOrgIds(@Param("surveyId") Long surveyId, @Param("orgIds") List<Long> orgIds);

    @Query("""
                SELECT new nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyOrgDetails(
                    mapping,
                    organisation,
                    parent,
                    grandParent
                )
                FROM SurveyOrgMappingEntity mapping
                LEFT JOIN mapping.organisation organisation
                LEFT JOIN organisation.parent parent
                LEFT JOIN parent.parent grandParent
                WHERE mapping.surveyId = :surveyId
            """)
    List<SurveyOrgDetails> findBySurveyIdWithHierarchy(@Param("surveyId") Long surveyId);

}
