package nl.ing.api.contacting.conf.domain.model.permission;

import java.util.Set;

public record PermissionOrganisationVO(
        int cltId,
        String cltName,
        int circleId,
        String circleName,
        int superCircleId,
        String superCircleName,
        Set<OrganisationalRestriction> organisationalRestriction
) {}