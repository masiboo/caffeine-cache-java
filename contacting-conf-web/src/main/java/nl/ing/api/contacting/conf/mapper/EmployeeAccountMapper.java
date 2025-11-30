package nl.ing.api.contacting.conf.mapper;


import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.cassandra.EmployeesByAccountEntity;
import nl.ing.api.contacting.conf.domain.model.permission.EmployeeAccountsVO;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class EmployeeAccountMapper {

    public static EmployeesByAccountEntity toEntity(EmployeeAccountsVO vo) {
        if (vo == null) {
            return null;
        }
        return EmployeesByAccountEntity.builder()
                .employeeId(vo.employeeId())
                .accountFriendlyName(vo.accountFriendlyName())
                .preferredAccount(vo.preferredAccount())
                .businessUnit(vo.businessUnit())
                .department(vo.department())
                .team(vo.team())
                .roles(vo.roles() != null ? vo.roles() : "")
                .organisationalRestrictions(vo.organisationalRestrictions() != null ? vo.organisationalRestrictions() : Set.of())
                .allowedChannels(vo.allowedChannels() != null ? vo.allowedChannels() : new HashMap<>())
                .workerSid(vo.workerSid())
                .build();
    }

    public static EmployeeAccountsVO toVO(EmployeesByAccountEntity entity) {
        if (entity == null) {
            return null;
        }
        return new EmployeeAccountsVO(
                entity.getEmployeeId(),
                entity.getAccountFriendlyName(),
                entity.isPreferredAccount(),
                entity.getRoles() != null ? entity.getRoles() : "",
                entity.getBusinessUnit(),
                entity.getDepartment(),
                entity.getTeam(),
                entity.getOrganisationalRestrictions() != null ? entity.getOrganisationalRestrictions() : Set.of(),
                entity.getAllowedChannels() != null ? entity.getAllowedChannels() : new HashMap<>(),
                entity.getWorkerSid()
        );
    }
}