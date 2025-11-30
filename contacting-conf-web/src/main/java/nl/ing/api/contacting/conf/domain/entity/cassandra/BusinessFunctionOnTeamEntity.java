package nl.ing.api.contacting.conf.domain.entity.cassandra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;

@Table("business_functions_on_teams")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessFunctionOnTeamEntity {

    @PrimaryKeyColumn(name = "account_friendly_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String accountFriendlyName;

    @PrimaryKeyColumn(name = "business_function", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String businessFunction;

    @PrimaryKeyColumn(name = "organisation_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private int organisationId;

    @PrimaryKeyColumn(name = "role", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    private String role;

    @Column("restriction")
    private String restriction;
}