package nl.ing.api.contacting.conf.domain.model.permission;

import java.util.List;
import java.util.Optional;

public record EmployeeBusinessFunctionVO(
        Optional<PermissionOrganisationVO> organisation,
        List<BusinessFunctionVO> businessFunctions

) implements PermissionBusinessFunctionVO {

    @Override
    public List<BusinessFunctionVO> getBusinessFunctions() {
        return businessFunctions;
    }

    @Override
    public Optional<PermissionOrganisationVO> getOrganisation() {
        return organisation;
    }
}