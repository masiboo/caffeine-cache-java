package nl.ing.api.contacting.conf.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Entity
@Table(
    name = "ORGANISATION_SETTINGS",
    indexes = {
        @Index(name = "ORG_ACC_SET_KEY", columnList = "ACCOUNT_ID,KEY,ORG_ID", unique = true)
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrganisationSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "ID")
    private Long id;

    @Column(name = "KEY", nullable = false)
    private String key;

    @Column(name = "VALUE", nullable = false)
    private String value;

    @EqualsAndHashCode.Include
    @Column(name = "ACCOUNT_ID", nullable = false)
    private Long accountId;

    @Column(name = "ORG_ID", nullable = false)
    private Long orgId;

    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled;

    @Column(name = "CAPABILITY")
    private String capabilities;
}
