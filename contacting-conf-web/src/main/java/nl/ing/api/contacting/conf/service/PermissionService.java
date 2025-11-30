package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.AuditContext;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.permission.BusinessFunctionDto;
import com.ing.api.contacting.dto.java.resource.permission.PermissionsDto;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import com.ing.apisdk.toolkit.logging.audit.api.ActionStatus;
import com.ing.apisdk.toolkit.logging.audit.api.AuditEvent;
import com.ing.apisdk.toolkit.logging.audit.api.Severity;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.configuration.AuditLoggerService;
import nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions;
import nl.ing.api.contacting.conf.domain.entity.cassandra.BusinessFunctionOnTeamEntity;
import nl.ing.api.contacting.conf.domain.entity.cassandra.ContactingConfigEntity;
import nl.ing.api.contacting.conf.domain.entity.cassandra.EmployeesByAccountEntity;
import nl.ing.api.contacting.conf.domain.enums.CONTACTING;
import nl.ing.api.contacting.conf.domain.enums.CUSTOMER_AUTHENTICATED;
import nl.ing.api.contacting.conf.domain.enums.CUSTOMER_UNAUTHENTICATED;
import nl.ing.api.contacting.conf.domain.enums.FOREIGN_API;
import nl.ing.api.contacting.conf.domain.model.permission.*;
import nl.ing.api.contacting.conf.mapper.BusinessFunctionMapper;
import nl.ing.api.contacting.conf.mapper.ContactingConfigMapper;
import nl.ing.api.contacting.conf.mapper.EmployeeAccountMapper;
import nl.ing.api.contacting.conf.repository.EmployeesByAccountRepository;
import nl.ing.api.contacting.conf.repository.PermissionCacheRepository;
import nl.ing.api.contacting.conf.util.PermissionUtils;
import nl.ing.api.contacting.trust.rest.context.*;
import nl.ing.api.contacting.trust.rest.feature.permissions.ACCOUNT$;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.ing.api.contacting.conf.domain.model.permission.OrganisationalRestrictionLevel.ACCOUNT;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class PermissionService {

    public static final int ORG_ID_FOR_ACCOUNT = -1;
    private static final String READONLY_BUSINESS_FUNCTIONS = "BUSINESS_FUNCTIONS_HIDDEN";
    public static final String BUSINESS_FUNCTIONS_AT_TEAM_LEVEL = "BUSINESS_FUNCTIONS_AT_TEAM_LEVEL";
    public static final String BUSINESS_FUNCTIONS = "BUSINESS_FUNCTIONS";
    public static final String ROLES = "ROLES";

    private final ContactingConfigService contactingConfigService;
    private final EmployeesByAccountRepository employeesByAccountRepository;
    private final PermissionCacheRepository permissionCacheRepository;
    private final AuditLoggerService auditLoggerService;


    public Map<String, Object> fetchPermissions(AuthorizationContext authContext, ContactingContext contactingContext, String accountFriendlyName) {
        List<BusinessFunctionVO> businessFunctionVOS = getAllBusinessFunctions(contactingContext, accountFriendlyName);
        return getPermissions(authContext, accountFriendlyName, businessFunctionVOS);

    }

    public Map<String, Object> getPermissions(AuthorizationContext authContext,
                                              String accountFriendlyName,
                                              List<BusinessFunctionVO> businessFunctions) {
        if (authContext instanceof EmployeeContext employeeContext) {
            return getPermissionsForEmployeeContext(employeeContext.employeeId(), accountFriendlyName, businessFunctions);
        }
        if (authContext instanceof CustomerContext) {
            return createNonEmployeePermissionVO(businessFunctions, Set.of(CUSTOMER_AUTHENTICATED.role()));
        }
        if (authContext instanceof ContactingApiContext) {
            return createNonEmployeePermissionVO(businessFunctions, Set.of(CONTACTING.role()));
        }
        if (authContext instanceof ForeignApiContext) {
            return createNonEmployeePermissionVO(businessFunctions, Set.of(FOREIGN_API.role()));
        }
        return createNonEmployeePermissionVO(businessFunctions, Set.of(CUSTOMER_UNAUTHENTICATED.role()));
    }

    Map<String, Object> getPermissionsForEmployeeContext(String employeeId,
                                                         String accountFriendlyName,
                                                         List<BusinessFunctionVO> businessFunctions) {
        return getPermissionsForEmployeeMap(employeeId, accountFriendlyName, businessFunctions, false);
    }

    public Map<String, Object> createNonEmployeePermissionVO(List<BusinessFunctionVO> businessFunctions,
                                                             Set<String> roles) {
        NonEmployeeBusinessFunctionVO nonEmployeeBusinessFunctionVO = new NonEmployeeBusinessFunctionVO(PermissionUtils.filterMaxRestrictions(businessFunctions, roles));
        return PermissionUtils.getPermissionsForEmployeeContextVO(Optional.empty(), nonEmployeeBusinessFunctionVO.businessFunctions());
    }


    public List<BusinessFunctionVO> getEditableBusinessFunctions(ContactingContext contactingContext, String accountFriendlyName) {
        List<BusinessFunctionOnTeamEntity> allBusinessFunctions = Optional.ofNullable(permissionCacheRepository.findByAccountFriendlyNameCache(contactingContext, accountFriendlyName))
                                                                    .orElse(Collections.emptyList());
        Set<String> readonlyBusinessFunctions = Optional.ofNullable(contactingConfigService.findByKey(READONLY_BUSINESS_FUNCTIONS))
                                                    .orElse(Collections.emptySet());
        return allBusinessFunctions.stream()
                .filter(bf -> !readonlyBusinessFunctions.contains(bf.getBusinessFunction()))
                .map(bf -> new BusinessFunctionVO(
                        bf.getAccountFriendlyName(),
                        bf.getBusinessFunction(),
                        bf.getRole(),
                        OrganisationalRestrictionLevel.fromValue(bf.getRestriction()),
                        bf.getOrganisationId()
                ))
                .sorted(Comparator.comparing(BusinessFunctionVO::businessFunction))
                .toList();
    }


    public void syncBusinessFunctions(ContactingContext contactingContext, List<BusinessFunctionVO> businessFunctions, AccountDto accountDto, AuditContext auditContext) {
        List<ContactingConfigEntity> allConfig = Optional.ofNullable(contactingConfigService.findAll())
                                                .orElse(Collections.emptyList());
        List<ContactingConfigVO> contactingConfigVOs = allConfig.stream()
                .map(ContactingConfigMapper::toVO)
                .toList();
        if (PermissionUtils.validate(contactingConfigVOs, businessFunctions)) {
            List<BusinessFunctionVO> editableBusinessFunctions = getEditableBusinessFunctions(contactingContext, accountDto.friendlyName());
            List<BusinessFunctionVO> removedRoles = removeRoles(accountDto.friendlyName(), editableBusinessFunctions, businessFunctions, auditContext);
            addRoles(contactingContext, accountDto.friendlyName(), removedRoles, businessFunctions, auditContext);
        }
    }

    @Transactional
    public List<BusinessFunctionVO> removeRoles(String accountFriendlyName,
                                                List<BusinessFunctionVO> currentPermissions,
                                                List<BusinessFunctionVO> newPermissionsRoleVO,
                                                AuditContext auditContext) {

        List<BusinessFunctionVO> toRemoveRoles = currentPermissions.stream()
                .filter(role -> !newPermissionsRoleVO.contains(role))
                .toList();

        if (toRemoveRoles.isEmpty()) {
            return List.of();
        }

        for (BusinessFunctionVO vo : toRemoveRoles) {
            permissionCacheRepository.deletePermission(vo, accountFriendlyName);
        }

        String removedRolesStr = toRemoveRoles.stream()
                .map(BusinessFunctionVO::role)
                .collect(Collectors.joining(","));


        auditLoggerService.logAuditEvent(new AuditEvent(Severity.MEDIUM,
                Instant.now(), auditContext.modifiedBy(),
                "BUSINESS_FUNCTIONS_MAPPINGS_REMOVED",
                removedRolesStr,
                ActionStatus.SUCCESS));

        return toRemoveRoles;
    }

    @Transactional
    public void addRoles(ContactingContext contactingContext,
                         String accountFriendlyName,
                         List<BusinessFunctionVO> currentPermissions,
                         List<BusinessFunctionVO> newPermissionsRoleVO,
                         AuditContext auditContext) {

        List<BusinessFunctionVO> toAddRoles = new ArrayList<>(newPermissionsRoleVO);
        toAddRoles.removeAll(currentPermissions);

        if (toAddRoles.isEmpty()) {
            return;
        }

        List<BusinessFunctionOnTeamEntity> entitiesToUpsert = BusinessFunctionMapper.toEntityList(toAddRoles);

        List<BusinessFunctionOnTeamEntity> updatedEntities = Optional.ofNullable(permissionCacheRepository.upsertAll(contactingContext, accountFriendlyName, entitiesToUpsert))
                                                            .orElse(Collections.emptyList());

        String addedRolesStr = toAddRoles.stream()
                .map(BusinessFunctionVO::role)
                .collect(Collectors.joining(","));

        auditLoggerService.logAuditEvent(new AuditEvent(Severity.MEDIUM,
                Instant.now(), auditContext.modifiedBy(),
                "BUSINESS_FUNCTIONS_MAPPINGS_ADDED",
                addedRolesStr,
                ActionStatus.SUCCESS));

        BusinessFunctionMapper.toVOList(updatedEntities);
    }

    public Map<String, Object> getPermissionsForEmployeeAndAccountFriendlyName(ContactingContext contactingContext, String employeeId, String accountFriendlyName) {
        List<BusinessFunctionVO> businessFunctionVOS = getAllBusinessFunctions(contactingContext, accountFriendlyName);
        return getPermissionsForEmployeeMap(employeeId, accountFriendlyName, businessFunctionVOS, true);
    }

    @Transactional
    protected List<BusinessFunctionVO> getAllBusinessFunctions(ContactingContext contactingContext, String accountFriendlyName) {

        List<BusinessFunctionOnTeamEntity> businessFunctionEntities =
                Optional.ofNullable(permissionCacheRepository.findByAccountFriendlyNameCache(contactingContext, accountFriendlyName))
                        .orElse(Collections.emptyList());

        // Convert entities to VO list
        List<BusinessFunctionVO> businessFunctionVOList =
                new ArrayList<>(BusinessFunctionMapper.toVOList(businessFunctionEntities));

        // Create systemTooling VO
        BusinessFunctionVO systemTooling = new BusinessFunctionVO(
                "",
                ContactingBusinessFunctions.SYSTEM_TOOLING,
                "CONTACTING",
                ACCOUNT,
                ORG_ID_FOR_ACCOUNT
        );

        businessFunctionVOList.add(0, systemTooling);

        return businessFunctionVOList;
    }

    public Map<String, Object> getPermissionsForEmployeeMap(
            String employeeId,
            String accountFriendlyName,
            List<BusinessFunctionVO> businessFunctionVOS,
            boolean isPermissionsForWithAndAccountFriendlyName) {

        Optional<EmployeesByAccountEntity> employeeAccountEntity =
                Optional.ofNullable(employeesByAccountRepository.findByEmployeeIdAndAccountFriendlyName(employeeId, accountFriendlyName))
                        .orElse(Optional.empty());
        EmployeeAccountsVO employeeAccountsVO = employeeAccountEntity
                .map(EmployeeAccountMapper::toVO)
                .orElse(null);

        return isPermissionsForWithAndAccountFriendlyName
                ? PermissionUtils.getPermissionsForEmployeeAndAccountFriendlyNameMap(Optional.ofNullable(employeeAccountsVO), businessFunctionVOS)
                : PermissionUtils.getPermissionsForEmployeeContextVO(Optional.ofNullable(employeeAccountsVO), businessFunctionVOS);

    }

    public static final PermissionsDto systemToolingPermission = new PermissionsDto(
            null,
            Set.of(),
            List.of(new BusinessFunctionDto(
                    ContactingBusinessFunctions.SYSTEM_TOOLING,
                    "CONTACTING",
                    ACCOUNT$.MODULE$.level(),
                    null
            ))
    );
}

