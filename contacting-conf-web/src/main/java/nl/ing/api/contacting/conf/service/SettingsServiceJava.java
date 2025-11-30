package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.employee.settings.EmployeeSettings;
import com.ing.api.contacting.dto.java.resource.employee.settings.OrganisationSetting;
import com.ing.api.contacting.dto.java.resource.employee.settings.Setting;
import com.ing.api.contacting.dto.java.resource.organisation.FlatOrganisationUnitDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.OrganisationSettingVO;
import nl.ing.api.contacting.conf.mapper.OrganisationSettingMapperJava;
import nl.ing.api.contacting.conf.mapper.SettingMapperJava;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsServiceJava {

    private final OrganisationSettingsServiceJava organisationSettingsService;
    private final AccountSettingsServiceJava accountSettingsService;

    /**
     * Returns all settings for an employee.
     */
    public EmployeeSettings getAllSettingsForEmployee(
            ContactingContext context,
            String accountFriendlyName,
            com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto primaryOrganisation
    ) {
        FlatOrganisationUnitDto primaryOrgJ = convertToJavaDto(primaryOrganisation);
        List<Setting> orgSettings = getOrgSettingsForEmployee(context, primaryOrgJ);
        AccountSettingsTuple accountSettingsTuple = getAccountSettingsForEmployee(context);

        return new EmployeeSettings(
                accountFriendlyName,
                new OrganisationSetting(primaryOrgJ.cltId(), orgSettings),
                accountSettingsTuple.accountSettings(),
                accountSettingsTuple.platformAccountSettings()
        );
    }

    private List<Setting> getOrgSettingsForEmployee(
            ContactingContext context,
            FlatOrganisationUnitDto primaryOrganisation
    ) {
        List<OrganisationSettingVO> allOrgSettings = organisationSettingsService.getOrganisationSettingsByOrgIds(
                OrganisationSettingMapperJava.orgDtoToOrgIdSet(primaryOrganisation), context.accountId()
        );
        return OrganisationSettingMapperJava.getSettingsForOrganisation(allOrgSettings, primaryOrganisation)
                .stream()
                .map(SettingMapperJava::toSettings)
                .toList();
    }

    private AccountSettingsTuple getAccountSettingsForEmployee(ContactingContext context) {
        var settings = accountSettingsService.getAllSettings(context);
        return new AccountSettingsTuple(
                settings.accountSettings().stream().map(SettingMapperJava::toSettings).toList(),
                settings.platformAccountSettings().stream().map(SettingMapperJava::toSettings).toList()
        );
    }

    private FlatOrganisationUnitDto convertToJavaDto(com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto scalaDto) {
        return new FlatOrganisationUnitDto(
                scalaDto.cltId(),
                scalaDto.cltName(),
                scalaDto.circleId(),
                scalaDto.circleName(),
                scalaDto.superCircleId(),
                scalaDto.superCircleName()
        );
    }

    private record AccountSettingsTuple(List<Setting> accountSettings, List<Setting> platformAccountSettings) {
    }
}
