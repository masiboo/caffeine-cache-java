package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.AccountSetting;
import com.ing.api.contacting.dto.java.resource.PlatformAccountSetting;
import com.ing.api.contacting.dto.java.resource.employee.settings.Setting;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.OrganisationSettingVO;

/**
 * Maps various setting types to Setting record.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SettingMapperJava {

    /**
     * Maps AccountSetting to Setting.
     */
    public static Setting toSettings(AccountSetting accountSetting) {
        return new Setting(accountSetting.key(), accountSetting.value());
    }

    /**
     * Maps PlatformAccountSetting to Setting.
     */
    public static Setting toSettings(PlatformAccountSetting platformAccountSetting) {
        return new Setting(platformAccountSetting.key(), platformAccountSetting.value());
    }

    /**
     * Maps OrganisationSettingVO to Setting.
     */
    public static Setting toSettings(OrganisationSettingVO organisationSetting) {
        return new Setting(organisationSetting.key(), organisationSetting.value());
    }
}
