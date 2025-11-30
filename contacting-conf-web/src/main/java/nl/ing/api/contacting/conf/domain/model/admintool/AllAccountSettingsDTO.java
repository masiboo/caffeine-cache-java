package nl.ing.api.contacting.conf.domain.model.admintool;

import com.ing.api.contacting.dto.java.resource.PlatformAccountSetting;
import java.util.List;

/**
 * DTO for all account settings
 */
public record AllAccountSettingsDTO(
        List<AccountSettingDTO> accountSettingsDto,
        List<PlatformAccountSetting> platformAccountSettings
) {}


