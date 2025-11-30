package nl.ing.api.contacting.conf.domain.model.settingsmetadata;

import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataEntity;
import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataOptionsEntity;

public record SettingsMetadataWithOptions(
        SettingsMetadataEntity metadata,
        SettingsMetadataOptionsEntity option
) {
}
