package nl.ing.api.contacting.conf.domain.model.settingsmetadata;

import java.util.Optional;

public record SettingsOptionsVO(
        Optional<Long> id,
        String value,
        String displayName
) {}

