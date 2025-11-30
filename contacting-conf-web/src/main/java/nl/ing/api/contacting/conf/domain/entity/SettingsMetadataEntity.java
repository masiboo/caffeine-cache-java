package nl.ing.api.contacting.conf.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.ing.api.contacting.conf.domain.InputTypeJava;

import java.util.List;

@Entity
@Table(name = "SETTINGS_METADATA",
        indexes = {
                @Index(name = "SETTINGS_NAME_IDX", columnList = "NAME", unique = true)
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", insertable = false, updatable = false)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "INPUT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private InputTypeJava inputType;

    @Column(name = "REGEX")
    private String regex;

    @Column(name = "CAPABILITY")
    private String capability;

    @Column(name = "CONSUMERS")
    private String consumers;

    @OneToMany(mappedBy = "settingsMetaData", cascade = CascadeType.ALL, fetch =  FetchType.LAZY)
    private List<SettingsMetadataOptionsEntity> options;
}
