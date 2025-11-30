package nl.ing.api.contacting.conf.domain.model.settingsmetadata;

import nl.ing.api.contacting.conf.exception.Errors;

import java.util.List;
import java.util.Optional;

/**
 * Migrated from Scala OrganisationSettingVO.
 * Represents an organisation setting.
 */
public record OrganisationSettingVO(
    Optional<Long> id,
    String key,
    String value,
    long accountId,
    long orgId,
    boolean enabled,
    List<SettingCapability> capability

) {
    public OrganisationSettingVO {
        if (key == null || key.isEmpty() || key.length() >= 255) {
            throw Errors.valueMissing("setting key length must be between 0 and 255");
        }
        // Defensive: never allow nulls for Optional or List
        id = id != null ? id : Optional.empty();
        capability = capability != null ? capability : List.of();
    }

    public OrganisationSettingVO withCapability(List<SettingCapability> capability) {
        return new OrganisationSettingVO(
                this.id,
                this.key,
                this.value,
                this.accountId,
                this.orgId,
                this.enabled,
                capability != null ? capability : List.of()
        );
    }

}