package nl.ing.api.contacting.conf.repository.cassandra;

import com.ing.api.contacting.dto.java.audit.AuditInfo;
import com.ing.api.contacting.dto.java.context.AuditContext;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.contacting.conf.domain.entity.cassandra.BusinessFunctionOnTeamEntity;
import nl.ing.api.contacting.conf.domain.entity.cassandra.ContactingConfigEntity;
import nl.ing.api.contacting.conf.domain.entity.cassandra.EmployeesByAccountEntity;
import nl.ing.api.contacting.conf.domain.model.permission.OrganisationalRestriction;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CassandraTestData {

    public static BusinessFunctionOnTeamEntity getBusinessFunctionOnTeamEntity(){
        return BusinessFunctionOnTeamEntity.builder()
                .accountFriendlyName("test-account")
                .businessFunction("business-function-1")
                .organisationId(123)
                .role("admin")
                .restriction("read-write")
                .build();
    }

    public static OrganisationalRestriction getOrganisationalRestriction(){
        return new OrganisationalRestriction(
                123, // cltId
                "cltName", // cltName
                1020, // circleId
                "circleName", // circleName
                987, // superCircleId
                "superCircleName", // superCircleName
                true // preferred
        );
    }

    public static Optional<EmployeesByAccountEntity> getEmployeesByAccountEntity(){
        return Optional.ofNullable(EmployeesByAccountEntity.builder()
                .employeeId("EMP001")
                .accountFriendlyName("test-account")
                .preferredAccount(true)
                .businessUnit("Engineering")
                .department("Software Development")
                .team("Backend Team")
                .roles("developer,admin")
                .organisationalRestrictions(Set.of(getOrganisationalRestriction()))
                .allowedChannels(Map.of("email", 1, "phone", 2))
                .workerSid("WK12345")
                .build());
    }

    public static ContactingConfigEntity getContactingConfigEntity(){
        return ContactingConfigEntity.builder()
                .values("value")
                .key("key")
                .build();

    }

    public static ContactingContext getContactingContext(){
        AuditInfo auditInfo = new AuditInfo( "modifiedBy", LocalDateTime.now());
        AuditContext auditContext = new AuditContext("auditContext", Optional.of(LocalDateTime.now()), Optional.of(12L), Optional.of(auditInfo) );
        return new ContactingContext(auditContext.accountId().get(), auditContext, false);
    }

}
