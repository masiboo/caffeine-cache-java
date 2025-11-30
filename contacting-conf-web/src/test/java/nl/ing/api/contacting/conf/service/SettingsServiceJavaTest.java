// contacting-conf-web/src/test/java/nl/ing/api/contacting/conf/service/SettingsServiceJavaTest.java
package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.AuditContext;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.AccountSetting;
import com.ing.api.contacting.dto.java.resource.AllAccountSettings;
import com.ing.api.contacting.dto.java.resource.PlatformAccountSetting;
import com.ing.api.contacting.dto.java.resource.employee.settings.EmployeeSettings;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.OrganisationSettingVO;
import com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsServiceJavaTest {

    @Mock
    OrganisationSettingsServiceJava organisationSettingsService;

    @Mock
    AccountSettingsServiceJava accountSettingsService;

    @InjectMocks
    SettingsServiceJava settingsServiceJava;

    private final ContactingContext context = new ContactingContext(123L, new AuditContext("user"));
    private final FlatOrganisationUnitDto primaryOrg =
            new FlatOrganisationUnitDto(1, "cltName", 2, "circleName", 3, "subCircleName");

    @Test
    @DisplayName("getAllSettingsForEmployee returns correct EmployeeSettings for normal flow")
    void normalFlow() {
        OrganisationSettingVO orgSettingVO = new OrganisationSettingVO(
                Optional.of(1L), "key", "value", 123L, 1L, true, List.of()
        );
        AccountSetting accountSetting = new AccountSetting(Optional.of(1L), "accKey", "accValue", 123L);
        PlatformAccountSetting platformSetting = new PlatformAccountSetting(Optional.of(1L), "platKey", "platValue", 123L);

        when(organisationSettingsService.getOrganisationSettingsByOrgIds(anySet(), anyLong()))
                .thenReturn(List.of(orgSettingVO));
        when(accountSettingsService.getAllSettings(any()))
                .thenReturn(new AllAccountSettings(List.of(accountSetting), List.of(platformSetting)));

        EmployeeSettings employeeSettings = settingsServiceJava.getAllSettingsForEmployee(context, "friendlyName", primaryOrg);

        assertAll(
                () -> assertEquals("friendlyName", employeeSettings.accountFriendlyName()),
                () -> assertEquals(primaryOrg.cltId(), employeeSettings.primaryTeamSettings().orgId()),
                () -> assertEquals("key", employeeSettings.primaryTeamSettings().setting().get(0).name()),
                () -> assertEquals("accKey", employeeSettings.accountSettings().get(0).name()),
                () -> assertEquals("platKey", employeeSettings.platformSettings().get(0).name())
        );
    }

    @Test
    @DisplayName("getAllSettingsForEmployee returns EmployeeSettings with empty org settings")
    void emptyOrgSettings() {
        AccountSetting accountSetting = new AccountSetting(Optional.of(1L), "accKey", "accValue", 123L);
        PlatformAccountSetting platformSetting = new PlatformAccountSetting(Optional.of(1L), "platKey", "platValue", 123L);

        when(organisationSettingsService.getOrganisationSettingsByOrgIds(anySet(), anyLong()))
                .thenReturn(List.of());
        when(accountSettingsService.getAllSettings(any()))
                .thenReturn(new AllAccountSettings(List.of(accountSetting), List.of(platformSetting)));

        EmployeeSettings employeeSettings = settingsServiceJava.getAllSettingsForEmployee(context, "friendlyName", primaryOrg);

        assertTrue(employeeSettings.primaryTeamSettings().setting().isEmpty());
        assertEquals("accKey", employeeSettings.accountSettings().get(0).name());
        assertEquals("platKey", employeeSettings.platformSettings().get(0).name());
    }

    @Test
    @DisplayName("getAllSettingsForEmployee returns EmployeeSettings with empty account settings")
    void emptyAccountSettings() {
        OrganisationSettingVO orgSettingVO = new OrganisationSettingVO(
                Optional.of(1L), "key", "value", 123L, 1L, true, List.of()
        );

        when(organisationSettingsService.getOrganisationSettingsByOrgIds(anySet(), anyLong()))
                .thenReturn(List.of(orgSettingVO));
        when(accountSettingsService.getAllSettings(any()))
                .thenReturn(new AllAccountSettings(List.of(), List.of()));

        EmployeeSettings employeeSettings = settingsServiceJava.getAllSettingsForEmployee(context, "friendlyName", primaryOrg);

        assertEquals("key", employeeSettings.primaryTeamSettings().setting().get(0).name());
        assertTrue(employeeSettings.accountSettings().isEmpty());
        assertTrue(employeeSettings.platformSettings().isEmpty());
    }

    @Test
    @DisplayName("getAllSettingsForEmployee propagates exception from organisationSettingsService")
    void organisationException() {
        when(organisationSettingsService.getOrganisationSettingsByOrgIds(anySet(), anyLong()))
                .thenThrow(new RuntimeException("org error"));
        assertThrows(RuntimeException.class, () ->
                settingsServiceJava.getAllSettingsForEmployee(context, "friendlyName", primaryOrg)
        );
    }

    @Test
    @DisplayName("getAllSettingsForEmployee propagates exception from accountSettingsService")
    void accountException() {
        OrganisationSettingVO orgSettingVO = new OrganisationSettingVO(
                Optional.of(1L), "key", "value", 123L, 1L, true, List.of()
        );
        when(organisationSettingsService.getOrganisationSettingsByOrgIds(anySet(), anyLong()))
                .thenReturn(List.of(orgSettingVO));
        when(accountSettingsService.getAllSettings(any()))
                .thenThrow(new RuntimeException("account error"));

        assertThrows(RuntimeException.class, () ->
                settingsServiceJava.getAllSettingsForEmployee(context, "friendlyName", primaryOrg)
        );
    }
}
