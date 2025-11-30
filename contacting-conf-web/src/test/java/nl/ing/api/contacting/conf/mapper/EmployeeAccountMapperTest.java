package nl.ing.api.contacting.conf.mapper;

import nl.ing.api.contacting.conf.domain.entity.cassandra.EmployeesByAccountEntity;
import nl.ing.api.contacting.conf.domain.model.permission.EmployeeAccountsVO;
import nl.ing.api.contacting.conf.helper.PermissionTestData;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeAccountMapperTest {

    @Test
    void testToEntityAndToVO_AllFields() {
        Map<String, Integer> allowedChannels = Map.of("CHANNEL_A", 1, "CHANNEL_B", 2);

        EmployeeAccountsVO vo  = PermissionTestData.getEmployeeAccountsVO();
        EmployeesByAccountEntity entity = EmployeeAccountMapper.toEntity(vo);

        assertEquals("emp123", entity.getEmployeeId());
        assertEquals("acc456", entity.getAccountFriendlyName());
        assertTrue(entity.isPreferredAccount());

        assertEquals( "ADMIN", entity.getRoles());
        assertEquals("BU1", entity.getBusinessUnit());
        assertEquals("Dept1", entity.getDepartment());
        assertEquals("Team1", entity.getTeam());
        assertEquals(allowedChannels, entity.getAllowedChannels());
        assertEquals("workerSid1", entity.getWorkerSid());

        EmployeeAccountsVO vo2 = EmployeeAccountMapper.toVO(entity);
        assertEquals(vo.employeeId(), vo2.employeeId());
        assertEquals(vo.accountFriendlyName(), vo2.accountFriendlyName());
        assertEquals(vo.preferredAccount(), vo2.preferredAccount());
        assertEquals(vo.roles(), vo2.roles());
        assertEquals(vo.businessUnit(), vo2.businessUnit());
        assertEquals(vo.department(), vo2.department());
        assertEquals(vo.team(), vo2.team());
        assertEquals(vo.organisationalRestrictions(), vo2.organisationalRestrictions());
        assertEquals(vo.allowedChannels(), vo2.allowedChannels());
        assertEquals(vo.workerSid(), vo2.workerSid());
    }

    @Test
    void testToEntity_NullAllowedChannels() {
        EmployeeAccountsVO vo = PermissionTestData.getEmployeeAccountsVO();

        EmployeesByAccountEntity entity = EmployeeAccountMapper.toEntity(vo);
        assertNotNull(entity.getAllowedChannels());
        assertFalse(entity.getAllowedChannels().isEmpty());
    }

    @Test
    void testToVO_NullAllowedChannels() {
        EmployeesByAccountEntity entity = EmployeesByAccountEntity.builder()
                .employeeId("emp123")
                .accountFriendlyName("acc456")
                .preferredAccount(false)
                .roles("roles")
                .businessUnit(null)
                .department(null)
                .team(null)
                .organisationalRestrictions(Set.of())
                .allowedChannels(null)
                .workerSid(null)
                .build();

        EmployeeAccountsVO vo = EmployeeAccountMapper.toVO(entity);
        assertNotNull(vo.allowedChannels());
        assertTrue(vo.allowedChannels().isEmpty());
    }
}