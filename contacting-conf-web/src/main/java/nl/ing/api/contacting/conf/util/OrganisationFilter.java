package nl.ing.api.contacting.conf.util;

import nl.ing.api.contacting.java.domain.OrganisationVO;
import nl.ing.api.contacting.trust.rest.context.EmployeeContext;
import nl.ing.api.contacting.trust.rest.context.SessionContext;
import nl.ing.api.contacting.trust.rest.context.TrustContext;
import nl.ing.api.contacting.trust.rest.feature.permissions.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OrganisationFilter {

    public static List<OrganisationVO> getAllowedOrganisations(SessionContext sessionContext, List<OrganisationVO> organisations) {
        TrustContext trustContext = sessionContext.trustContext();
        if (trustContext instanceof EmployeeContext employeeContext) {
            OrganisationalRestrictionLevel restriction = getMaxRestrictionLevel(sessionContext);

            if (restriction.level() == SELF.level() || restriction.level() == NONE.level()) {
                return List.of();
            } else if (restriction.level() == ACCOUNT.level()) {
                return organisations;
            } else {
                Set<Long> superCircleIds = employeeContext.getRestrictionPerOrganisation().stream()
                        .map(restrictionOrganisationalUnitDto -> Long.valueOf(restrictionOrganisationalUnitDto.org().superCircleId()))
                        .collect(Collectors.toSet());

                List<OrganisationVO> superCirclesForUser = organisations.stream()
                        .filter(organisationVO -> organisationVO.id().isPresent() && superCircleIds.contains(organisationVO.id().get()))
                        .toList();

                if (restriction.level() <= CIRCLE.level()) {
                    return superCirclesForUser.stream()
                            .map(superCircle -> superCircle.withChildren(getRestrictedCircles(superCircle, employeeContext, restriction)))
                            .toList();
                } else {
                    return superCirclesForUser;
                }
            }
        } else {
            return organisations;
        }
    }

    private static OrganisationalRestrictionLevel getMaxRestrictionLevel(SessionContext sessionContext) {
        return sessionContext.getEmployeeContext()
                .map(employeeContext -> {
                    Set<RestrictionOrganisationalUnitDto> restrictions =
                            scala.jdk.javaapi.CollectionConverters.asJava(employeeContext.restrictionPerOrganisation());
                    if (restrictions.isEmpty()) {
                        return employeeContext.organisationalRestrictionLevel();
                    } else {
                        return restrictions.stream()
                                .max(Comparator.comparingInt(restrictionOrganisationalUnitDto -> restrictionOrganisationalUnitDto.restriction().level()))
                                .map(RestrictionOrganisationalUnitDto::restriction)
                                .orElse(OrganisationalRestrictionLevel.fromLevel(NONE.level()));
                    }
                })
                .orElse(OrganisationalRestrictionLevel.fromLevel(NONE.level()));
    }

    private static List<OrganisationVO> getRestrictedCircles(OrganisationVO superCircle, EmployeeContext employeeContext, OrganisationalRestrictionLevel restriction) {
        List<Long> circleIds = employeeContext.getRestrictionPerOrganisation().stream()
                .map(restrictionOrganisationalUnitDto -> Long.valueOf(restrictionOrganisationalUnitDto.org().circleId()))
                .toList();

        List<OrganisationVO> circlesForUser = superCircle.children().stream()
                .filter(circle -> circle.id().isPresent() && circleIds.contains(circle.id().get()))
                .toList();

        if (restriction.level() == TEAM.level()) {
            return getCirclesWithRestrictedTeams(employeeContext, circlesForUser);
        }
        return circlesForUser;
    }

    private static List<OrganisationVO> getCirclesWithRestrictedTeams(EmployeeContext employeeContext, List<OrganisationVO> circlesForUser) {
        Set<Long> teamIds = employeeContext.getRestrictionPerOrganisation().stream()
                .map(restrictionOrganisationalUnitDto -> Long.valueOf(restrictionOrganisationalUnitDto.org().cltId()))
                .collect(Collectors.toSet());

        return circlesForUser.stream()
                .map(circle -> {
                    List<OrganisationVO> filteredTeams = circle.children().stream()
                            .filter(team -> team.id().isPresent() && teamIds.contains(team.id().get()))
                            .toList();
                    return circle.withChildren(filteredTeams);
                })
                .toList();
    }
}