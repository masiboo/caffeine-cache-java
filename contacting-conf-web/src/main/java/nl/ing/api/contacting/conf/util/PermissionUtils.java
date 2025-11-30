package nl.ing.api.contacting.conf.util;

import nl.ing.api.contacting.conf.domain.model.permission.BusinessFunctionVO;
import nl.ing.api.contacting.conf.domain.model.permission.ContactingConfigVO;
import nl.ing.api.contacting.conf.domain.model.permission.EmployeeAccountsVO;
import nl.ing.api.contacting.conf.domain.model.permission.EmployeeBusinessFunctionVO;
import nl.ing.api.contacting.conf.domain.model.permission.OrganisationalRestriction;
import nl.ing.api.contacting.conf.domain.model.permission.PermissionOrganisationVO;
import nl.ing.api.contacting.conf.exception.Errors;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.ing.api.contacting.conf.service.PermissionService.*;


public class PermissionUtils {

    private static final String BUSINESS_FUNCTION = "real time dashboard";
    private static final String ROLE = "AGENT";
    private static final int ACCESS_RESTRICTION = 2;

    public static Map<String, Object> getPermissionsForEmployeeContextVO(
            Optional<EmployeeAccountsVO> employee,
            List<BusinessFunctionVO> businessFunctions) {

        Map<String, Object> response = new LinkedHashMap<>();

        // Employee Context
        if (employee != null && employee.isPresent()) {
            EmployeeAccountsVO emp = employee.get();
            Set<OrganisationalRestriction> restrictions = getSortedRestrictions(emp);
            Optional<OrganisationalRestriction> preferredTeamOpt = getPreferredTeam(restrictions);

            Optional<PermissionOrganisationVO> organisationVO = preferredTeamOpt.map(team ->
                    new PermissionOrganisationVO(
                            team.cltId(), team.cltName(),
                            team.circleId(), team.circleName(),
                            team.superCircleId(), team.superCircleName(),
                            restrictions
                    )
            );

            Object organisationalRestrictions = preferredTeamOpt
                    .map(PermissionUtils::buildOrganisationMap)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());

            response.put("organisationalRestrictions", organisationalRestrictions);

            EmployeeBusinessFunctionVO employeeBusinessFunctionVO =
                    new EmployeeBusinessFunctionVO(organisationVO, getFilteredBusinessFunctions(businessFunctions, emp.getRolesAsSet()));

            response.put("businessFunctions", buildBusinessFunctionList(employeeBusinessFunctionVO.getBusinessFunctions()));
        } else {
            // NonEmployee Context
            response.put("organisationalRestrictions", Collections.emptyList());
            response.put("businessFunctions", PermissionUtils.buildBusinessFunctionList(businessFunctions));
        }

        return response;
    }


    private static Map<String, Object> buildOrganisationMap(OrganisationalRestriction restriction) {
        Map<String, Object> organisationMap = new LinkedHashMap<>();
        organisationMap.put("cltId", restriction.cltId());
        organisationMap.put("cltName", restriction.cltName());
        organisationMap.put("circleId", restriction.circleId());
        organisationMap.put("circleName", restriction.circleName());
        organisationMap.put("superCircleId", restriction.superCircleId());
        organisationMap.put("superCircleName", restriction.superCircleName());
        return organisationMap;
    }

    private static List<Map<String, Object>> buildBusinessFunctionList(List<BusinessFunctionVO> businessFunctions) {
        return businessFunctions.stream().map(businessFunction -> {
            Map<String, Object> functionMap = new LinkedHashMap<>();
            functionMap.put("businessFunction", businessFunction.businessFunction());
            functionMap.put("role", businessFunction.role());
            functionMap.put("accessRestriction", businessFunction.restriction().getLevel());
            if (BUSINESS_FUNCTION.equals(businessFunction.businessFunction()) &&
                    ROLE.equals(businessFunction.role()) &&
                    ACCESS_RESTRICTION == businessFunction.restriction().getLevel()) {
                functionMap.put("organisationId", businessFunction.organisationId());
            }
            return functionMap;
        }).toList();
    }

    public static Map<String, Object> getPermissionsForEmployeeAndAccountFriendlyNameMap(
            Optional<EmployeeAccountsVO> employee,
            List<BusinessFunctionVO> businessFunctions) {

        Map<String, Object> response = new LinkedHashMap<>();

        if (employee.isPresent()) {
            EmployeeAccountsVO emp = employee.get();
            Set<OrganisationalRestriction> restrictions = getSortedRestrictions(emp);
            Optional<OrganisationalRestriction> preferredTeamOpt = getPreferredTeam(restrictions);

            // Add organisation
            if (preferredTeamOpt.isPresent()) {
                Map<String, Object> organisationMap = buildOrganisationMap(preferredTeamOpt.get());
                response.put("organisation", organisationMap);
            } else {
                response.put("organisation", Map.of());
            }


            // Add organisationalRestrictions (without preferred flag)
            List<Map<String, Object>> restrictionsList = restrictions.stream()
                    .map(r -> {
                        Map<String, Object> restrictionMap = new LinkedHashMap<>();
                        restrictionMap.put("cltId", r.cltId());
                        restrictionMap.put("cltName", r.cltName());
                        restrictionMap.put("circleId", r.circleId());
                        restrictionMap.put("circleName", r.circleName());
                        restrictionMap.put("superCircleId", r.superCircleId());
                        restrictionMap.put("superCircleName", r.superCircleName());
                        return restrictionMap;
                    }).toList();
            response.put("organisationalRestrictions", restrictionsList);

            // Add businessFunctions
            List<Map<String, Object>> bfList = filterMaxRestrictions(businessFunctions, emp.getRolesAsSet()).stream()
                    .map(bf -> {
                                Map<String, Object> restrictionMap = new LinkedHashMap<>();
                                restrictionMap.put("businessFunction", bf.businessFunction());
                                restrictionMap.put("role", bf.role());
                                restrictionMap.put("accessRestriction", bf.restriction().getLevel());
                                return restrictionMap;
                            }
                    ).toList();
            response.put("businessFunctions", bfList);

        } else {
            response.put("organisation", Map.of());
            response.put("organisationalRestrictions", List.of());
            response.put("businessFunctions", getFilteredBusinessFunctions(businessFunctions, Set.of()).stream()
                    .map(bf -> Map.of(
                            "businessFunction", bf.businessFunction(),
                            "role", bf.role(),
                            "accessRestriction", bf.restriction().getLevel()
                    )).toList());
        }
        return response;
    }

    private static Set<OrganisationalRestriction> getSortedRestrictions(EmployeeAccountsVO emp) {
        return emp.organisationalRestrictions().stream()
                .sorted(Comparator.comparing(OrganisationalRestriction::cltId).reversed())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Optional<OrganisationalRestriction> getPreferredTeam(Set<OrganisationalRestriction> restrictions) {
        return restrictions.stream()
                .filter(OrganisationalRestriction::preferred)
                .findFirst();
    }

    private static List<BusinessFunctionVO> getFilteredBusinessFunctions(List<BusinessFunctionVO> businessFunctions, Set<String> roles) {
        return filterMaxRestrictions(businessFunctions, roles);
    }

    public static List<BusinessFunctionVO> filterMaxRestrictions(
            List<BusinessFunctionVO> businessFunctions,
            Set<String> roles) {

        List<BusinessFunctionVO> allowed = filterAllowed(businessFunctions, roles);

        Map<GroupKey, List<BusinessFunctionVO>> grouped = allowed.stream()
                .collect(Collectors.groupingBy(bf -> new GroupKey(bf.businessFunction(), bf.organisationId())));

        return grouped.values().stream()
                .map(list -> Collections.max(list, Comparator.comparingInt(bf -> bf.restriction().getLevel())))
                .toList();
    }

    private static List<BusinessFunctionVO> filterAllowed(
            List<BusinessFunctionVO> businessFunctions,
            Set<String> roles) {
        return businessFunctions.stream()
                .filter(bf -> roles.contains(bf.role()))
                .toList();
    }

    public static boolean validate(List<ContactingConfigVO> config, List<BusinessFunctionVO> newPermissionsRoleVO) {
        // Extract valid business functions, team-level business functions, and roles from config
        Set<String> validBusinessFunctions = config.stream()
                .filter(cf -> BUSINESS_FUNCTIONS.equals(cf.key()))
                .flatMap(c -> c.valuesAsSet().stream())
                .collect(Collectors.toSet());

        Set<String> validTeamLevelBusinessFunctions = config.stream()
                .filter(cf -> BUSINESS_FUNCTIONS_AT_TEAM_LEVEL.equals(cf.key()))
                .flatMap(c -> c.valuesAsSet().stream())
                .collect(Collectors.toSet());

        Set<String> validRoles = config.stream()
                .filter(cf -> ROLES.equals(cf.key()))
                .flatMap(c -> c.valuesAsSet().stream())
                .collect(Collectors.toSet());

        // Validate business functions
        boolean allBusinessFunctionsValid = newPermissionsRoleVO.stream()
                .allMatch(x -> validBusinessFunctions.contains(x.businessFunction()));
        if (!allBusinessFunctionsValid) {
            throw Errors.badRequest("Updating permissions with an invalid business function");
        }

        // Validate roles
        List<String> invalidRoles = newPermissionsRoleVO.stream()
                .map(BusinessFunctionVO::role)
                .filter(role -> !validRoles.contains(role))
                .toList();
        if (!invalidRoles.isEmpty()) {
            throw Errors.badRequest("Updating permissions with an invalid role: " + String.join(",", invalidRoles));
        }

        // Validate organisationId and team-level permissions
        boolean allAccountLevel = newPermissionsRoleVO.stream()
                .allMatch(vo -> vo.organisationId() == ORG_ID_FOR_ACCOUNT);

        boolean anyTeamLevelValid = newPermissionsRoleVO.stream()
                .filter(vo -> vo.organisationId() != ORG_ID_FOR_ACCOUNT)
                .anyMatch(vo -> validTeamLevelBusinessFunctions.contains(vo.businessFunction()));

        if (!allAccountLevel && !anyTeamLevelValid) {
             throw  Errors.badRequest("Permission at team level are only allowed for " + validTeamLevelBusinessFunctions.toString().trim());
        }
        return true;
    }

}