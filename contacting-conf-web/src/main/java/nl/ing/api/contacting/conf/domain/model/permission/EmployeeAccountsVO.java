package nl.ing.api.contacting.conf.domain.model.permission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record EmployeeAccountsVO(
    String employeeId,
    String accountFriendlyName,
    boolean preferredAccount,
    String roles,
    String businessUnit,
    String department,
    String team,
    Set<OrganisationalRestriction> organisationalRestrictions,
    Map<String, Integer> allowedChannels,
    String workerSid
) {
    // Compact constructor with default values
    public EmployeeAccountsVO {
        // Initialize collections if null
        organisationalRestrictions = organisationalRestrictions != null ? organisationalRestrictions : new HashSet<>();
        allowedChannels = allowedChannels != null ? allowedChannels : new HashMap<>();
    }

    // Business method
    public Set<String> getRolesAsSet() {
        return roles != null ? Set.of(roles.split(",")) : Set.of();
    }
}