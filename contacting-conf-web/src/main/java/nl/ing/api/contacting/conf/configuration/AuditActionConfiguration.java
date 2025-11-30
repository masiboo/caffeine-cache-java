package nl.ing.api.contacting.conf.configuration;

import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.entity.OrganisationSettingsEntity;
import nl.ing.api.contacting.java.repository.jpa.audit.AuditEntityActions;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Make this class Generic to avoid for each entity
 */
@Configuration
public class AuditActionConfiguration {

    @Bean
    public AuditEntityActions<AccountSettingsEntity, Long> accountSetting() {
        return new AuditEntityActions<>() {

            @Override
            protected String getEntityTag() {
                return "ACCOUNT_SETTINGS";
            }

            @Override
            protected Optional<Long> getEntityId(AccountSettingsEntity entity) {
                return Optional.of(entity.getId());
            }

            @Override
            protected Class<AccountSettingsEntity> getEntityClass() {
                return AccountSettingsEntity.class;
            }

            @Override
            protected Class<Long> getIdClass() {
                return Long.class;
            }
        };
    }

    @Bean
    public AuditEntityActions<OrganisationSettingsEntity, Long> organisationSetting() {
        return new AuditEntityActions<>() {

            @Override
            protected String getEntityTag() {
                return "ORGANISATION_SETTINGS";
            }

            @Override
            protected Optional<Long> getEntityId(OrganisationSettingsEntity entity) {
                return Optional.of(entity.getId());
            }

            @Override
            protected Class<OrganisationSettingsEntity> getEntityClass() {
                return OrganisationSettingsEntity.class;
            }

            @Override
            protected Class<Long> getIdClass() {
                return Long.class;
            }
        };
    }

    @Bean
    public AuditEntityActions<OrganisationEntity, Long> organisation() {
        return new AuditEntityActions<>() {

            @Override
            protected String getEntityTag() {
                return "ORGANISATIONS";
            }

            @Override
            protected Optional<Long> getEntityId(OrganisationEntity entity) {
                return Optional.of(entity.getId());
            }

            @Override
            protected Class<OrganisationEntity> getEntityClass() {
                return OrganisationEntity.class;
            }

            @Override
            protected Class<Long> getIdClass() {
                return Long.class;
            }
        };
    }
}
