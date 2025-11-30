package nl.ing.api.contacting.conf.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "ACCOUNT_SETTINGS",
        indexes = {@Index(name = "ACCOUNT_SETTINGS_KEY_VALUE", columnList = "KEY,ACCOUNT_ID", unique = true)})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AccountSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "ID", insertable = false, updatable = false)
    private Long id;

    @Column(name = "KEY", nullable = false)
    private String key;

    @Column(name = "VALUE", nullable = false)
    private String value;

    @Column(name = "CAPABILITY")
    private String capabilities;

    @Column(name = "CONSUMERS")
    private String consumers;

    @EqualsAndHashCode.Include
    @Column(name = "ACCOUNT_ID", nullable = false)
    private Long accountId;
}
