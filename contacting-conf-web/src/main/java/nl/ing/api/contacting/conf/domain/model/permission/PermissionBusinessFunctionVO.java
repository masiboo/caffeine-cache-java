package nl.ing.api.contacting.conf.domain.model.permission;

import java.util.List;
import java.util.Optional;

public interface PermissionBusinessFunctionVO {
    List<BusinessFunctionVO> getBusinessFunctions();
    Optional<PermissionOrganisationVO> getOrganisation();
}