package nl.ing.api.contacting.conf.helper;

import com.ing.api.contacting.dto.java.resource.organisation.BusinessFunctionAccess;
import com.ing.api.contacting.dto.java.resource.organisation.BusinessFunctionsDto;
import nl.ing.api.contacting.conf.domain.model.permission.*;

import java.util.*;

public class PermissionTestData {

    public static List<BusinessFunctionsDto> getBusinessFunctionsDto() {
        return List.of(
                new BusinessFunctionsDto(
                        "CHAT",
                        List.of(
                                new BusinessFunctionAccess("ADMIN", 2, 1),
                                new BusinessFunctionAccess("AGENT", 1, 2)
                        )
                ),
                new BusinessFunctionsDto(
                        "CASE_MANAGEMENT",
                        List.of(
                                new BusinessFunctionAccess("ADMIN", 2, 1),
                                new BusinessFunctionAccess("SUPERVISOR", 2, 2)
                        )
                ),
                new BusinessFunctionsDto(
                        "CUSTOMER_VIEW",
                        List.of(
                                new BusinessFunctionAccess("AGENT", 1, 2),
                                new BusinessFunctionAccess("CUSTOMER_AUTHENTICATED", 0, 3)
                        )
                )
        );
    }

    public static EmployeeBusinessFunctionVO getEmployeeBusinessFunctionVO() {
        return new EmployeeBusinessFunctionVO(
                Optional.of(new PermissionOrganisationVO(
                        101,                    // cltId
                        "Test CLT",            // cltName
                        201,                    // circleId
                        "Test Circle",         // circleName
                        301,                    // superCircleId
                        "Super Circle",        // superCircleName
                        Set.of(new OrganisationalRestriction(
                                1, "cltA", 10, "circleA", 100, "superCircleA", true
                        ))
                )),
                List.of(
                        new BusinessFunctionVO(
                                "test-account",
                                "CHAT",
                                "ADMIN",
                                OrganisationalRestrictionLevel.TEAM,
                                1
                        ),
                        new BusinessFunctionVO(
                                "test-account",
                                "CASE_MANAGEMENT",
                                "SUPERVISOR",
                                OrganisationalRestrictionLevel.CIRCLE,
                                2
                        ),
                        new BusinessFunctionVO(
                                "test-account",
                                "CUSTOMER_VIEW",
                                "AGENT",
                                OrganisationalRestrictionLevel.SELF,
                                3
                        )
                )
        );
    }

    public static EmployeeAccountsVO getEmployeeAccountsVO() {
        Set<OrganisationalRestriction> restrictions = Set.of(
                new OrganisationalRestriction(1, "cltA", 10, "circleA", 100, "superCircleA", true)
        );
        Map<String, Integer> allowedChannels = Map.of("CHANNEL_A", 1, "CHANNEL_B", 2);

        return new EmployeeAccountsVO(
                "emp123",
                "acc456",
                true,
                "ADMIN",
                "BU1",
                "Dept1",
                "Team1",
                restrictions,
                allowedChannels,
                "workerSid1"
        );
    }

    public  static Map<String, Object> getResponseMap() {
        Map<String, Object> response = new HashMap<>();
        response.put("businessFunctions", getBusinessFunctionsDto());
        return  response;
    }

    public static  Map<String, Object> getNonEmployeePermissionMap(){

        Map<String, Object> dummyResponse = new LinkedHashMap<>();

        dummyResponse.put("organisation", Collections.emptyMap());

        List<Map<String, Object>> dummyBusinessFunctions = List.of(
                Map.of("businessFunction", "dummyFunction1", "role", "DUMMY_ROLE", "accessRestriction", 1),
                Map.of("businessFunction", "dummyFunction2", "role", "DUMMY_ROLE", "accessRestriction", 2)
        );

        dummyResponse.put("businessFunctions", dummyBusinessFunctions);
        return dummyResponse;

    }


}