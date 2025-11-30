package nl.ing.api.contacting.conf.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SETTINGS_METADATA_OPTIONS",
        indexes = @Index(name = "SETTINGS_OPTIONS_IDX",
                columnList = "SETTINGS_META_ID,VALUE",
                unique = true))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SettingsMetadataOptionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", insertable = false, updatable = false)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "VALUE", nullable = false)
    private String value;

    @Column(name = "DISPLAY_NAME", nullable = false)
    private String displayName;

    @EqualsAndHashCode.Include
    @Column(name = "SETTINGS_META_ID", nullable = false)
    private Long settingsMetaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SETTINGS_META_ID", insertable = false, updatable = false)
    private SettingsMetadataEntity settingsMetaData;
}
