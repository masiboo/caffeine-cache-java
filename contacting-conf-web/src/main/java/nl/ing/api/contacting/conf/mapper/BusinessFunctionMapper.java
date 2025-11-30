package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.organisation.BusinessFunctionAccess;
import com.ing.api.contacting.dto.java.resource.organisation.BusinessFunctionsDto;
import com.ing.api.contacting.dto.java.resource.permission.BusinessFunctionDto;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.cassandra.BusinessFunctionOnTeamEntity;
import nl.ing.api.contacting.conf.domain.model.permission.BusinessFunctionVO;
import nl.ing.api.contacting.conf.domain.model.permission.FunctionOrgKey;
import nl.ing.api.contacting.conf.domain.model.permission.OrganisationalRestrictionLevel;
import nl.ing.api.contacting.conf.service.PermissionService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class BusinessFunctionMapper {

    public static BusinessFunctionOnTeamEntity toEntity(BusinessFunctionVO vo) {
        if (vo == null) {
            return null;
        }
        return BusinessFunctionOnTeamEntity.builder()
                .accountFriendlyName(vo.accountFriendlyName())
                .businessFunction(vo.businessFunction())
                .role(vo.role())
                .organisationId(vo.organisationId())
                .restriction(vo.restriction() != null ? vo.restriction().getValue() : null)
                .build();
    }

    public static BusinessFunctionVO toVO(BusinessFunctionOnTeamEntity entity) {
        if (entity == null) {
            return null;
        }
        return new BusinessFunctionVO(
                entity.getAccountFriendlyName(),
                entity.getBusinessFunction(),
                entity.getRole(),
                entity.getRestriction() != null ? OrganisationalRestrictionLevel.fromValue(entity.getRestriction()) : null,
                entity.getOrganisationId()
        );
    }

    public static BusinessFunctionsDto toDto(BusinessFunctionVO vo) {
        if (vo == null) {
            return null;
        }
        BusinessFunctionAccess access = new BusinessFunctionAccess(
                vo.role(),
                vo.restriction().getLevel(),
                vo.organisationId()
        );
        return new BusinessFunctionsDto(
                vo.accountFriendlyName(),
                List.of(access)
        );
    }

    public static List<BusinessFunctionVO> toVOList(List<BusinessFunctionOnTeamEntity> businessFunctionEntityList) {
        if (businessFunctionEntityList == null) {
            return List.of();
        }
        return businessFunctionEntityList.stream()
                .filter(java.util.Objects::nonNull)
                .map(BusinessFunctionMapper::toVO)
                .toList();
    }

    public static List<BusinessFunctionOnTeamEntity> toEntityList(List<BusinessFunctionVO> businessFunctionVOS) {
        if (businessFunctionVOS == null) {
            return List.of();
        }
        return businessFunctionVOS.stream()
                .filter(java.util.Objects::nonNull)
                .map(BusinessFunctionMapper::toEntity)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public static List<BusinessFunctionVO> toBusinessFunctionVOList(String accountFriendlyName,
                                                                    BusinessFunctionsDto businessFunctionsDto) {
        if (businessFunctionsDto == null) {
            return List.of();
        }
        if (businessFunctionsDto.allowedAccess() == null) {
            return List.of(new BusinessFunctionVO(
                    accountFriendlyName,
                    businessFunctionsDto.name(),
                    null,
                    OrganisationalRestrictionLevel.NONE,
                    PermissionService.ORG_ID_FOR_ACCOUNT
            ));
        }
        return businessFunctionsDto.allowedAccess().stream()
                .filter(java.util.Objects::nonNull)
                .map(access -> new BusinessFunctionVO(
                        accountFriendlyName,
                        businessFunctionsDto.name(),
                        access.role(),
                        OrganisationalRestrictionLevel.fromLevel(access.level()),
                        access.organisationId().orElseGet(() -> PermissionService.ORG_ID_FOR_ACCOUNT)
                ))
                .toList();
    }

    public static List<BusinessFunctionsDto> toDtoList(List<BusinessFunctionVO> businessFunctionVOS) {
        if (businessFunctionVOS == null) {
            return List.of();
        }
        List<BusinessFunctionDto> dtos = businessFunctionVOS.stream()
                .filter(java.util.Objects::nonNull)
                .map(BusinessFunctionMapper::voToDto)
                .toList();

        if (dtos.isEmpty()) {
            return List.of();
        }

        Map<FunctionOrgKey, List<BusinessFunctionDto>> grouped = dtos.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(dto ->
                        new FunctionOrgKey(
                                dto.businessFunction(),
                                dto.organisationId() != null ? dto.organisationId() : -1
                        )
                ));

        if (grouped.isEmpty()) {
            return List.of();
        }

        return grouped.entrySet().stream()
                .filter(java.util.Objects::nonNull)
                .map(entry -> new BusinessFunctionsDto(
                        entry.getKey() != null ? entry.getKey().businessFunction() : null,
                        BusinessFunctionsDto.toAccessLevelByRoleDto(
                                entry.getValue(),
                                entry.getKey() != null && entry.getKey().organisationId() != null && entry.getKey().organisationId().equals(-1) ? null : (entry.getKey() != null ? entry.getKey().organisationId() : null)
                        )
                ))
                .toList();
    }

    private static BusinessFunctionDto voToDto(BusinessFunctionVO vo) {
        // Map VO to DTO, handling orgId for account
        Integer orgId = vo.organisationId() == PermissionService.ORG_ID_FOR_ACCOUNT ? null : vo.organisationId();
        return new BusinessFunctionDto(
                vo.businessFunction(),
                vo.role(),
                vo.restriction().getLevel(),
                orgId
        );
    }
}