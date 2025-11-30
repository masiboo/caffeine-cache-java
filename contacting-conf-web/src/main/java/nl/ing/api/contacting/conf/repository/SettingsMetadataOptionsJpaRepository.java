package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataOptionsEntity;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataWithOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingsMetadataOptionsJpaRepository extends JpaRepository<SettingsMetadataOptionsEntity, Long> {

    @Query("""
        SELECT new nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataWithOptions(m, o)
        FROM SettingsMetadataEntity m
        LEFT JOIN SettingsMetadataOptionsEntity o ON o.settingsMetaId = m.id
        """)
    List<SettingsMetadataWithOptions> findAllWithMetadata();

    @Query("""
        SELECT new nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataWithOptions(m, o)
        FROM SettingsMetadataEntity m
        LEFT JOIN SettingsMetadataOptionsEntity o ON o.settingsMetaId = m.id
        WHERE m.name = :name
        """)
    List<SettingsMetadataWithOptions> findByMetadataName(@Param("name") String name);
}
