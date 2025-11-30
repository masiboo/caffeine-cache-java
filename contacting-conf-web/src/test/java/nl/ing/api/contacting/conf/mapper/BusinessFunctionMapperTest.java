package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.organisation.BusinessFunctionAccess;
import com.ing.api.contacting.dto.java.resource.organisation.BusinessFunctionsDto;
import nl.ing.api.contacting.conf.domain.entity.cassandra.BusinessFunctionOnTeamEntity;
import nl.ing.api.contacting.conf.domain.model.permission.BusinessFunctionVO;
import nl.ing.api.contacting.conf.domain.model.permission.OrganisationalRestrictionLevel;
import nl.ing.api.contacting.conf.service.PermissionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BusinessFunctionMapperTest {

    @Test
    void testToEntityAndToVO() {
        BusinessFunctionVO vo = new BusinessFunctionVO(
                "acc1",
                "FUNC_A",
                "ROLE_X",
                OrganisationalRestrictionLevel.TEAM,
                100
        );

        BusinessFunctionOnTeamEntity entity = BusinessFunctionMapper.toEntity(vo);
        assertEquals("acc1", entity.getAccountFriendlyName());
        assertEquals("FUNC_A", entity.getBusinessFunction());
        assertEquals("ROLE_X", entity.getRole());
        assertEquals("TEAM", entity.getRestriction());
        assertEquals(100, entity.getOrganisationId());

        BusinessFunctionVO vo2 = BusinessFunctionMapper.toVO(entity);
        assertEquals(vo.accountFriendlyName(), vo2.accountFriendlyName());
        assertEquals(vo.businessFunction(), vo2.businessFunction());
        assertEquals(vo.role(), vo2.role());
        assertEquals(vo.restriction(), vo2.restriction());
        assertEquals(vo.organisationId(), vo2.organisationId());
    }

    @Test
    void testToVOListAndToEntityList() {
        BusinessFunctionOnTeamEntity entity1 = BusinessFunctionOnTeamEntity.builder()
                .accountFriendlyName("acc2")
                .businessFunction("FUNC_B")
                .role("ROLE_Y")
                .organisationId(200)
                .restriction("SELF")
                .build();

        BusinessFunctionOnTeamEntity entity2 = BusinessFunctionOnTeamEntity.builder()
                .accountFriendlyName("acc3")
                .businessFunction("FUNC_C")
                .role("ROLE_Z")
                .organisationId(300)
                .restriction("TEAM")
                .build();

        List<BusinessFunctionVO> voList = BusinessFunctionMapper.toVOList(List.of(entity1, entity2));
        assertEquals(2, voList.size());
        assertEquals("FUNC_B", voList.get(0).businessFunction());
        assertEquals("FUNC_C", voList.get(1).businessFunction());

        List<BusinessFunctionOnTeamEntity> entityList = BusinessFunctionMapper.toEntityList(voList);
        assertEquals(2, entityList.size());
        assertEquals("acc2", entityList.get(0).getAccountFriendlyName());
        assertEquals("acc3", entityList.get(1).getAccountFriendlyName());
    }

    @Test
    void testToVOListAndToEntityListWhenNull() {
        List<BusinessFunctionVO> voList = BusinessFunctionMapper.toVOList(null);
        assertEquals(0, voList.size());


        List<BusinessFunctionOnTeamEntity> entityList = BusinessFunctionMapper.toEntityList(voList);
        assertEquals(0, entityList.size());
    }

    @Test
    void testToDto() {
        BusinessFunctionVO vo = new BusinessFunctionVO(
                "acc1",
                "FUNC_A",
                "ROLE_X",
                OrganisationalRestrictionLevel.TEAM,
                100
        );

        BusinessFunctionsDto dto = BusinessFunctionMapper.toDto(vo);

        assertEquals("acc1", dto.name());
        assertEquals(1, dto.allowedAccess().size());

        BusinessFunctionAccess access = dto.allowedAccess().get(0);
        assertEquals("ROLE_X", access.role());
        assertEquals(OrganisationalRestrictionLevel.TEAM.getLevel(), access.level());
        assertEquals(100, access.organisationId().orElse(-1));
    }

    @Test
    void testToBusinessFunctionVOList_NullAllowedAccess() {
        BusinessFunctionsDto dto = new BusinessFunctionsDto("FUNC_X", null);
        var result = BusinessFunctionMapper.toBusinessFunctionVOList("accX", dto);
        assertEquals(1, result.size());
        BusinessFunctionVO vo = result.get(0);
        assertEquals("accX", vo.accountFriendlyName());
        assertEquals("FUNC_X", vo.businessFunction());
        assertNull(vo.role());
        assertEquals(OrganisationalRestrictionLevel.NONE, vo.restriction());
        assertEquals(PermissionService.ORG_ID_FOR_ACCOUNT, vo.organisationId());
    }

    @Test
    void testToBusinessFunctionVOList_WithAllowedAccess() {
        BusinessFunctionAccess access1 = new BusinessFunctionAccess("ROLE_A", 2, Optional.of(123));
        BusinessFunctionAccess access2 = new BusinessFunctionAccess("ROLE_B", 1, Optional.empty());
        BusinessFunctionsDto dto = new BusinessFunctionsDto("FUNC_Y", List.of(access1, access2));
        var result = BusinessFunctionMapper.toBusinessFunctionVOList("accY", dto);
        assertEquals(2, result.size());
        assertEquals("ROLE_A", result.get(0).role());
        assertEquals(OrganisationalRestrictionLevel.fromLevel(2), result.get(0).restriction());
        assertEquals(123, result.get(0).organisationId());
        assertEquals("ROLE_B", result.get(1).role());
        assertEquals(OrganisationalRestrictionLevel.fromLevel(1), result.get(1).restriction());
        assertEquals(PermissionService.ORG_ID_FOR_ACCOUNT, result.get(1).organisationId());
    }

    @Test
    void testToDtoList_Grouping() {
        BusinessFunctionVO vo1 = new BusinessFunctionVO("acc1", "FUNC_A", "ROLE_X", OrganisationalRestrictionLevel.TEAM, 100);
        BusinessFunctionVO vo2 = new BusinessFunctionVO("acc2", "FUNC_A", "ROLE_Y", OrganisationalRestrictionLevel.SELF, 100);
        BusinessFunctionVO vo3 = new BusinessFunctionVO("acc3", "FUNC_B", "ROLE_Z", OrganisationalRestrictionLevel.NONE, 200);

        var dtoList = BusinessFunctionMapper.toDtoList(List.of(vo1, vo2, vo3));
        assertEquals(2, dtoList.size());
        Assertions.assertTrue(dtoList.stream().anyMatch(dto -> dto.name().equals("FUNC_A")));
        Assertions.assertTrue(dtoList.stream().anyMatch(dto -> dto.name().equals("FUNC_B")));
    }
}