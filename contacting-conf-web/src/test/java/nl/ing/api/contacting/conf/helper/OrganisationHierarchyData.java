package nl.ing.api.contacting.conf.helper;

import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationLevelEnumeration;

import java.util.ArrayList;
import java.util.List;

public class OrganisationHierarchyData {

    public static List<OrganisationEntity[]> getOrgTree(Long accountId) {
        List<OrganisationEntity[]> orgTree = new ArrayList<>();

        // Super Circle SC1
        OrganisationEntity sc1 = OrganisationEntity.builder()
                .id(1L)
                .name("SC1")
                .accountId(accountId)
                .parentId(null)
                .orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE)
                .build();

        // Circles under SC1
        OrganisationEntity c1 = OrganisationEntity.builder()
                .id(2L)
                .name("C1")
                .accountId(accountId)
                .parentId(sc1.getId())
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();

        OrganisationEntity c2 = OrganisationEntity.builder()
                .id(3L)
                .name("C3")
                .accountId(accountId)
                .parentId(sc1.getId())
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();

        // CLTs under C1
        OrganisationEntity clt1 = OrganisationEntity.builder()
                .id(4L)
                .name("CLT1")
                .accountId(accountId)
                .parentId(c1.getId())
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        OrganisationEntity clt2 = OrganisationEntity.builder()
                .id(5L)
                .name("CLT2")
                .accountId(accountId)
                .parentId(c1.getId())
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        // CLTs under C2
        OrganisationEntity clt3 = OrganisationEntity.builder()
                .id(6L)
                .name("CLT3")
                .accountId(accountId)
                .parentId(c2.getId())
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        OrganisationEntity clt4 = OrganisationEntity.builder()
                .id(7L)
                .name("CLT4")
                .accountId(accountId)
                .parentId(c2.getId())
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        OrganisationEntity clt5 = OrganisationEntity.builder()
                .id(8L)
                .name("CLT5")
                .accountId(accountId)
                .parentId(c2.getId())
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        // Super Circle SC_1
        OrganisationEntity sc_1 = OrganisationEntity.builder()
                .id(11L)
                .name("SC_1")
                .accountId(accountId)
                .parentId(null)
                .orgLevel(OrganisationLevelEnumeration.SUPER_CIRCLE)
                .build();

        // Circles under SC_1
        OrganisationEntity c_1 = OrganisationEntity.builder()
                .id(12L)
                .name("C_1")
                .accountId(accountId)
                .parentId(sc_1.getId())
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();

        OrganisationEntity c_2 = OrganisationEntity.builder()
                .id(13L)
                .name("C_3")
                .accountId(accountId)
                .parentId(sc_1.getId())
                .orgLevel(OrganisationLevelEnumeration.CIRCLE)
                .build();

        // CLTs under C_1
        OrganisationEntity clt_1 = OrganisationEntity.builder()
                .id(14L)
                .name("CLT_1")
                .accountId(accountId)
                .parentId(c_1.getId())
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        OrganisationEntity clt_2 = OrganisationEntity.builder()
                .id(15L)
                .name("CLT_2")
                .accountId(accountId)
                .parentId(c_1.getId())
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        OrganisationEntity clt_3 = OrganisationEntity.builder()
                .id(16L)
                .name("CLT_3")
                .accountId(accountId)
                .parentId(c_1.getId())
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        // CLTs under C_2
        OrganisationEntity clt_4 = OrganisationEntity.builder()
                .id(17L)
                .name("CLT_4")
                .accountId(accountId)
                .parentId(c_2.getId())
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        OrganisationEntity clt_5 = OrganisationEntity.builder()
                .id(18L)
                .name("CLT_5")
                .accountId(accountId)
                .parentId(c_2.getId())
                .orgLevel(OrganisationLevelEnumeration.CLT)
                .build();

        // Build orgTree using OrganisationEntity[]
        orgTree.add(new OrganisationEntity[]{sc1, c1, clt1});
        orgTree.add(new OrganisationEntity[]{sc1, c1, clt2});
        orgTree.add(new OrganisationEntity[]{sc1, c2, clt3});
        orgTree.add(new OrganisationEntity[]{sc1, c2, clt4});
        orgTree.add(new OrganisationEntity[]{sc1, c2, clt5});

        orgTree.add(new OrganisationEntity[]{sc_1, c_1, clt_1});
        orgTree.add(new OrganisationEntity[]{sc_1, c_1, clt_2});
        orgTree.add(new OrganisationEntity[]{sc_1, c_2, clt_3});
        orgTree.add(new OrganisationEntity[]{sc_1, c_2, clt_4});
        orgTree.add(new OrganisationEntity[]{sc_1, c_2, clt_5});

        return orgTree;
    }
}
