package nl.ing.api.contacting.conf.util;


import nl.ing.api.contacting.conf.domain.model.permission.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static nl.ing.api.contacting.conf.service.PermissionService.*;
import static org.junit.jupiter.api.Assertions.*;

class PermissionUtilsTest {

    private OrganisationalRestriction restriction(int cltId, boolean preferred) {
        return new OrganisationalRestriction(cltId, "clt" + cltId, cltId + 10, "circle" + cltId, cltId + 100, "superCircle" + cltId, preferred);
    }

    private BusinessFunctionVO bf(String acc, String func, String role, OrganisationalRestrictionLevel level, int orgId) {
        return new BusinessFunctionVO(acc, func, role, level, orgId);
    }

    @Test
    void getPermissionsForEmployeeContextVO_employee_present() {
        Set<OrganisationalRestriction> restrictions = Set.of(restriction(2, false), restriction(1, true));
        EmployeeAccountsVO emp = new EmployeeAccountsVO("emp1", "acc1", true, "ADMIN,USER", "BU", "Dept", "Team", restrictions, Map.of("CHANNEL_A", 1), "sid");
        List<BusinessFunctionVO> bfs = List.of(
                bf("acc1", "FUNC_A", "ADMIN", OrganisationalRestrictionLevel.TEAM, 10),
                bf("acc1", "FUNC_B", "USER", OrganisationalRestrictionLevel.ACCOUNT, ORG_ID_FOR_ACCOUNT)
        );
        Map<String, Object> result = PermissionUtils.getPermissionsForEmployeeContextVO(Optional.of(emp), bfs);
        var organisationalRestrictions = result.get("organisationalRestrictions");
        assertEquals(2, result.size());
        assertNotNull(organisationalRestrictions);
        assertEquals("clt1", ((LinkedHashMap<?, ?>) ((List<?>) organisationalRestrictions).get(0)).get("cltName"));
        assertEquals("superCircle1", ((LinkedHashMap<?, ?>) ((List<?>) organisationalRestrictions).get(0)).get("superCircleName"));

    }

    @Test
    void getPermissionsForEmployeeContextVO_employee_absent() {
        List<BusinessFunctionVO> bfs = List.of(
                bf("acc1", "FUNC_A", "ADMIN", OrganisationalRestrictionLevel.TEAM, 10)
        );

        Map<String, Object> response = PermissionUtils.getPermissionsForEmployeeContextVO(Optional.empty(), bfs);

        assertNotNull(response.get("organisationalRestrictions"));
        assertNotNull(response.get("businessFunctions"));
    }

    @Test
    void getPermissionsForEmployeeContextMap_employee_present() {
        Set<OrganisationalRestriction> restrictions = Set.of(restriction(2, false), restriction(1, true));
        EmployeeAccountsVO emp = new EmployeeAccountsVO("emp1", "acc1", true, "ADMIN", "BU", "Dept", "Team", restrictions, Map.of(), "sid");
        List<BusinessFunctionVO> bfs = List.of(
                bf("acc1", "FUNC_A", "ADMIN", OrganisationalRestrictionLevel.TEAM, 10)
        );
        Map<String, Object> map = PermissionUtils.getPermissionsForEmployeeContextVO(Optional.of(emp), bfs);
        assertTrue(map.containsKey("organisationalRestrictions"));
        assertTrue(map.containsKey("businessFunctions"));
    }

    @Test
    void getPermissionsForEmployeeContextMap_employee_absent() {
        List<BusinessFunctionVO> bfs = List.of(
                bf("acc1", "FUNC_A", "ADMIN", OrganisationalRestrictionLevel.TEAM, 10)
        );
        Map<String, Object> map = PermissionUtils.getPermissionsForEmployeeAndAccountFriendlyNameMap(Optional.empty(), bfs);
        assertEquals(Map.of(), map.get("organisation"));
        assertEquals(List.of(), map.get("organisationalRestrictions"));
        assertTrue(map.containsKey("businessFunctions"));
    }

    @Test
    void filterMaxRestrictions_returns_max_level_per_group() {
        List<BusinessFunctionVO> bfs = List.of(
                bf("acc1", "FUNC_A", "ADMIN", OrganisationalRestrictionLevel.TEAM, 10),
                bf("acc1", "FUNC_A", "ADMIN", OrganisationalRestrictionLevel.ACCOUNT, 10),
                bf("acc1", "FUNC_B", "USER", OrganisationalRestrictionLevel.TEAM, 20)
        );
        Set<String> roles = Set.of("ADMIN", "USER");
        List<BusinessFunctionVO> filtered = PermissionUtils.filterMaxRestrictions(bfs, roles);
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(bf -> bf.businessFunction().equals("FUNC_B")));
    }

    @Test
    void filterMaxRestrictions_empty_roles_returns_empty() {
        List<BusinessFunctionVO> bfs = List.of(
                bf("acc1", "FUNC_A", "ADMIN", OrganisationalRestrictionLevel.TEAM, 10)
        );
        List<BusinessFunctionVO> filtered = PermissionUtils.filterMaxRestrictions(bfs, Set.of());
        assertTrue(filtered.isEmpty());
    }

    @Test
    void validate_valid_config_returns_true() {
        List<ContactingConfigVO> config = List.of(
                new ContactingConfigVO(BUSINESS_FUNCTIONS, "FUNC_A,FUNC_B"),
                new ContactingConfigVO(BUSINESS_FUNCTIONS_AT_TEAM_LEVEL, "FUNC_A"),
                new ContactingConfigVO(ROLES, "ADMIN,USER")
        );
        List<BusinessFunctionVO> bfs = List.of(
                bf("acc1", "FUNC_A", "ADMIN", OrganisationalRestrictionLevel.TEAM, 10),
                bf("acc1", "FUNC_B", "USER", OrganisationalRestrictionLevel.ACCOUNT, ORG_ID_FOR_ACCOUNT)
        );
        assertTrue(PermissionUtils.validate(config, bfs));
    }

    @Test
    void validate_invalid_business_function_throws() {
        List<ContactingConfigVO> config = List.of(
                new ContactingConfigVO(BUSINESS_FUNCTIONS, "FUNC_A"),
                new ContactingConfigVO(BUSINESS_FUNCTIONS_AT_TEAM_LEVEL, "FUNC_A"),
                new ContactingConfigVO(ROLES, "ADMIN")
        );
        List<BusinessFunctionVO> bfs = List.of(
                bf("acc1", "INVALID_FUNC", "ADMIN", OrganisationalRestrictionLevel.TEAM, 10)
        );
        Exception ex = assertThrows(RuntimeException.class, () -> PermissionUtils.validate(config, bfs));
        assertTrue(ex.getMessage().contains("invalid business function"));
    }

    @Test
    void validate_invalid_role_throws() {
        List<ContactingConfigVO> config = List.of(
                new ContactingConfigVO(BUSINESS_FUNCTIONS, "FUNC_A"),
                new ContactingConfigVO(BUSINESS_FUNCTIONS_AT_TEAM_LEVEL, "FUNC_A"),
                new ContactingConfigVO(ROLES, "ADMIN")
        );
        List<BusinessFunctionVO> bfs = List.of(
                bf("acc1", "FUNC_A", "NOT_A_ROLE", OrganisationalRestrictionLevel.TEAM, 10)
        );
        Exception ex = assertThrows(RuntimeException.class, () -> PermissionUtils.validate(config, bfs));
        assertTrue(ex.getMessage().contains("invalid role"));
    }

    @Test
    void validate_invalid_team_level_throws() {
        List<ContactingConfigVO> config = List.of(
                new ContactingConfigVO(BUSINESS_FUNCTIONS, "FUNC_A"),
                new ContactingConfigVO(BUSINESS_FUNCTIONS_AT_TEAM_LEVEL, "FUNC_A"),
                new ContactingConfigVO(ROLES, "ADMIN")
        );
        List<BusinessFunctionVO> bfs = List.of(
                bf("acc1", "FUNC_B", "ADMIN", OrganisationalRestrictionLevel.TEAM, 10)
        );
        Exception ex = assertThrows(RuntimeException.class, () -> PermissionUtils.validate(config, bfs));
        assertTrue(ex.getMessage().contains("Updating permissions with an invalid business function"));
    }
}