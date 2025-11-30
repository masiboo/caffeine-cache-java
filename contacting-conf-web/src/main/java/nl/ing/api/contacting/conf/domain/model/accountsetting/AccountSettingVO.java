package nl.ing.api.contacting.conf.domain.model.accountsetting;

import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;

import java.util.List;
import java.util.Optional;

public record AccountSettingVO(
        Optional<Long> id,
        String key,
        String value,
        List<SettingCapability> capability,
        List<AccountSettingConsumers> consumers,
        Long accountId
) {
    public AccountSettingVO withId(Optional<Long> newId) {
        return new AccountSettingVO(
                newId,
                this.key,
                this.value,
                this.capability,
                this.consumers,
                this.accountId
        );
    }
}
