package nl.ing.api.contacting.conf.util;

import com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto;
import nl.ing.api.contacting.conf.helper.OrganisationHierarchyData;
import nl.ing.api.contacting.conf.mapper.OrganisationMapperJava;
import nl.ing.api.contacting.java.domain.OrganisationVO;
import nl.ing.api.contacting.trust.rest.context.EmployeeContext;
import nl.ing.api.contacting.trust.rest.context.SessionContext;
import nl.ing.api.contacting.trust.rest.feature.permissions.OrganisationalRestrictionLevel;
import nl.ing.api.contacting.trust.rest.feature.permissions.RestrictionOrganisationalUnitDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import scala.Option;
import scala.collection.immutable.Set;
import scala.collection.immutable.Set$;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrganisationFilterTest {

    private List<OrganisationVO> orgTree;

    @BeforeEach
    void setUp() {
        var hierarchy = OrganisationHierarchyData.getOrgTree(1L);
        orgTree = OrganisationMapperJava.organisationHierarchyToVo(hierarchy);
    }

    @Test
    @DisplayName("shouldGiveBackIdsUnderAccountForWorkerWithMultipleTeams")
    void shouldGiveBackIdsUnderAccountForWorkerWithMultipleTeams() {
        // Arrange
        FlatOrganisationUnitDto orgDto1 = new FlatOrganisationUnitDto(8, "CLT5", 3, "C2", 1, "SC1");
        FlatOrganisationUnitDto orgDto2 = new FlatOrganisationUnitDto(16, "CLT_3", 12, "C_1", 11, "SC_1");

        // Scala immutable Set for restrictions
        Set<RestrictionOrganisationalUnitDto> restrictions = Set$.MODULE$.<RestrictionOrganisationalUnitDto>empty()
                .$plus(new RestrictionOrganisationalUnitDto(orgDto1, "ADMIN", OrganisationalRestrictionLevel.fromLevel(5)))
                .$plus(new RestrictionOrganisationalUnitDto(orgDto1, "ADMIN", OrganisationalRestrictionLevel.fromLevel(2)));

        EmployeeContext employeeContext = new EmployeeContext(
                "123",
                "123",
                Option.apply(orgDto2),
                OrganisationalRestrictionLevel.fromLevel(5),
                Option.empty(),
                Option.empty(),
                restrictions
        );

        SessionContext sessionContext = new SessionContext(employeeContext);

        // Act
        List<OrganisationVO> allowedOrganisations = OrganisationFilter.getAllowedOrganisations(sessionContext, orgTree);
        List<Long> result = flattenIds(allowedOrganisations);

        // Assert
        List<Long> expected = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L);
        assertEquals(expected, result, "Expected all organisation IDs under account");
    }

    @Test
    @DisplayName("shouldRetrieveAllOrganisationsWhenUserHasNoRestrictions")
    void shouldRetrieveAllOrganisationsWhenUserHasNoRestrictions() {
        // Arrange
        FlatOrganisationUnitDto orgDto1 = new FlatOrganisationUnitDto(8, "CLT5", 3, "C2", 1, "SC1");
        Set<RestrictionOrganisationalUnitDto> restrictions = Set$.MODULE$.<RestrictionOrganisationalUnitDto>empty();

        EmployeeContext employeeContext = new EmployeeContext(
                "123",
                "123",
                Option.apply(orgDto1),
                OrganisationalRestrictionLevel.fromLevel(5),
                Option.empty(),
                Option.empty(),
                restrictions
        );

        SessionContext sessionContext = new SessionContext(employeeContext);

        // Act
        List<OrganisationVO> allowedOrgs = OrganisationFilter.getAllowedOrganisations(sessionContext, orgTree);
        List<Long> resultIds = flattenIds(allowedOrgs);

        // Assert
        List<Long> expected = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L);
        assertEquals(expected, resultIds, "Expected all organisation IDs under account");
    }
    @Test
    void shouldRetrieveNoOrganisationIdsWhenUserHasNoRestrictions() {
        FlatOrganisationUnitDto orgDto1 = new FlatOrganisationUnitDto(8, "CLT5", 3, "C2", 1, "SC1");
        // Empty Scala Set for restrictions
        Set<RestrictionOrganisationalUnitDto> restrictions = Set$.MODULE$.<RestrictionOrganisationalUnitDto>empty();
        EmployeeContext employeeContext = new EmployeeContext(
                "123", // sessionId
                "123", // employeeId
                Option.apply(orgDto1),
                OrganisationalRestrictionLevel.fromLevel(5), // fallback level
                Option.empty(),
                Option.empty(),
                restrictions
        );
        SessionContext sessionContext = new SessionContext(employeeContext);
        List<OrganisationVO> result = OrganisationFilter.getAllowedOrganisations(sessionContext,List.of());
        assertTrue(result.isEmpty(), "Expected no organisation IDs when org tree is empty and no restrictions");
    }


    // Flattens the organisation tree to a sorted list of IDs
    private static List<Long> flattenIds(List<OrganisationVO> roots) {
        return roots.stream()
                .flatMap(root -> flattenNode(root).stream())
                .sorted()
                .toList();
    }

    // Recursively collects IDs from the organisation tree
    private static List<Long> flattenNode(OrganisationVO node) {
        List<Long> ids = new ArrayList<>();
        node.id().ifPresent(ids::add);
        node.children().forEach(child -> ids.addAll(flattenNode(child)));
        return ids;
    }

    @Test
    void shouldRetrieveOrganisationIdsForSupercircleAllCirclesAndTeamsIfLevelIsSupercircle() {
        // Arrange
        FlatOrganisationUnitDto orgDto1 = new FlatOrganisationUnitDto(8, "CLT5", 3, "C2", 1, "SC1");
        Set<RestrictionOrganisationalUnitDto> restrictions = Set$.MODULE$.<RestrictionOrganisationalUnitDto>empty()
                .$plus(new RestrictionOrganisationalUnitDto(orgDto1, "ADMIN", OrganisationalRestrictionLevel.fromLevel(4)));

        EmployeeContext employeeContext = new EmployeeContext(
                "123", // sessionId
                "123", // employeeId
                Option.apply(orgDto1),
                OrganisationalRestrictionLevel.fromLevel(5), // fallback level
                Option.empty(),
                Option.empty(),
                restrictions
        );

        SessionContext sessionContext = new SessionContext(employeeContext);
        // Act
        List<OrganisationVO> allowedOrgs = OrganisationFilter.getAllowedOrganisations(sessionContext, orgTree);
        // Flatten IDs
        List<Long> resultIds = flattenIds(allowedOrgs);
      //  resultIds.sort(Long::compare);
        List<Long> expected = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);
        assertEquals(expected, resultIds, "Expected all organisation IDs under supercircle");
    }

    @Test
    void shouldRetrieveOrganisationIdsForCircleAndTeamsIfLevelIsCircle() {
        // Arrange
        FlatOrganisationUnitDto orgDto1 = new FlatOrganisationUnitDto(8, "CLT5", 3, "C2", 1, "SC1");
        Set<RestrictionOrganisationalUnitDto> restrictions = Set$.MODULE$.<RestrictionOrganisationalUnitDto>empty()
                .$plus(new RestrictionOrganisationalUnitDto(orgDto1, "ADMIN", OrganisationalRestrictionLevel.fromLevel(3)));

        EmployeeContext employeeContext = new EmployeeContext(
                "123", // sessionId
                "123", // employeeId
                Option.apply(orgDto1),
                OrganisationalRestrictionLevel.fromLevel(5), // fallback level
                Option.empty(),
                Option.empty(),
                restrictions
        );

        SessionContext sessionContext = new SessionContext(employeeContext);
        List<OrganisationVO> allowedOrgs = OrganisationFilter.getAllowedOrganisations(sessionContext, orgTree);
        List<Long> resultIds = flattenIds(allowedOrgs);
        List<Long> expected = List.of(1L,3L, 6L, 7L, 8L);
        assertEquals(expected, resultIds, "Expected circle and all teams under it when restriction level is CIRCLE");
    }

    @Test
    void shouldRetrieveOrganisationIdsForSingleTeamIfLevelIsTeam() {
        // Arrange
        FlatOrganisationUnitDto orgDto1 = new FlatOrganisationUnitDto(8, "CLT5", 3, "C2", 1, "SC1");
        // Restrictions: TEAM level
        Set<RestrictionOrganisationalUnitDto> restrictions = Set$.MODULE$.<RestrictionOrganisationalUnitDto>empty()
                .$plus(new RestrictionOrganisationalUnitDto(orgDto1, "ADMIN", OrganisationalRestrictionLevel.fromLevel(2)));
        EmployeeContext employeeContext = new EmployeeContext(
                "123", // sessionId
                "123", // employeeId
                Option.apply(orgDto1),
                OrganisationalRestrictionLevel.fromLevel(5), // fallback level
                Option.empty(),
                Option.empty(),
                restrictions
        );
        SessionContext sessionContext = new SessionContext(employeeContext);
        // Act
        List<OrganisationVO> allowedOrgs = OrganisationFilter.getAllowedOrganisations(sessionContext, orgTree);
        // Flatten IDs
        List<Long> resultIds = flattenIds(allowedOrgs);
        // Assert
        List<Long> expected = List.of(1L,3L,8L);
        assertEquals(expected, resultIds, "Expected only the team ID when restriction level is TEAM");
    }
}