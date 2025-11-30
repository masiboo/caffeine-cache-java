package nl.ing.api.contacting.conf.domain.entity.cassandra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.ing.api.contacting.conf.domain.model.permission.OrganisationalRestriction;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.util.Map;
import java.util.Set;

@Table("employees_by_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeesByAccountEntity {

    @PrimaryKeyColumn(name = "employee_id", type = PrimaryKeyType.PARTITIONED)
    private String employeeId;

    @PrimaryKeyColumn(name = "account_friendly_name", type = PrimaryKeyType.CLUSTERED)
    private String accountFriendlyName;

    @Column("preferred_account")
    private boolean preferredAccount;

    @Column("business_unit")
    private String businessUnit;

    @Column("department")
    private String department;

    @Column("team")
    private String team;

    @Column("roles")
    private String roles;

    @Column("organisational_restrictions")
    private Set<OrganisationalRestriction> organisationalRestrictions;

    @Column("allowed_channels")
    private Map<String, Integer> allowedChannels;

    @Column("worker_sid")
    private String workerSid;
}