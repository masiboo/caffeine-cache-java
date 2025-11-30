package nl.ing.api.contacting.conf.domain.model.admintool;

import com.ing.api.contacting.dto.java.resource.PlatformAccountSetting;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingVO;
import nl.ing.api.contacting.conf.mapper.AccountSettingsMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents all account settings for admin tool.
 */
public record AllAccountSettingsVO(
        List<AccountSettingVO> accountSettings,
        List<PlatformAccountSetting> platformAccountSettings
) {
    /**
     * Converts to DTO
     *
     * @return DTO representation
     */
    public AllAccountSettingsDTO toDTO() {
        return new AllAccountSettingsDTO(
                accountSettings.stream()
                        .map(AccountSettingsMapper::toDTO)
                        .toList(),
                platformAccountSettings
        );
    }
}

