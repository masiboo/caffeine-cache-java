package nl.ing.api.contacting.conf.domain.model.settingsmetadata;

import nl.ing.api.contacting.conf.domain.InputTypeJava;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingConsumers;

import java.util.List;
import java.util.Optional;

public record SettingsMetadataVO(
        Optional<Long> id,
        String name,
        InputTypeJava inputType,
        Optional<String> regex,
        List<SettingsOptionsVO> options,
        List<SettingCapability> capability,
        List<AccountSettingConsumers> consumers
) {}
