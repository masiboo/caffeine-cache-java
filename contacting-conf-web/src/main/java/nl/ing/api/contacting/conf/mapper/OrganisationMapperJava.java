package nl.ing.api.contacting.conf.mapper;

import com.ing.api.contacting.dto.java.resource.organisation.OrganisationDto;
import com.ing.api.contacting.dto.java.resource.organisation.OrganisationSaveDto;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import nl.ing.api.contacting.java.domain.OrganisationVO;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationEntity;
import nl.ing.api.contacting.java.repository.model.organisation.OrganisationLevelEnumeration;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class OrganisationMapperJava {

    /**
     * Maps OrganisationVO to OrganisationDto.
     * If OrganisationDto is extended to support levelName, add vo.organisationLevel().getName() as a field.
     */
    public static OrganisationDto toDto(OrganisationVO vo) {
        return new OrganisationDto(
                vo.id().orElse(0L),
                vo.name(),
                vo.organisationLevel().getId(),
                // If OrganisationDto supports levelName, add: vo.organisationLevel().getName(),
                vo.parent().map(OrganisationMapperJava::toDto),
                vo.children().stream().map(OrganisationMapperJava::toDto).toList()
        );
    }

    /**
     * Maps OrganisationSaveDtoJava and accountId to OrganisationEntity.
     * If OrganisationSaveDto is extended to support levelName, use OrganisationLevelEnumeration.withName(dto.levelName())
     * for mapping by name. Currently, only id-based mapping is supported.
     */
    public static OrganisationEntity toEntity(@NotNull OrganisationSaveDto dto, long accountId) {
        // If dto.levelName() is added in the future, prefer mapping by name:
        // OrganisationLevelEnumeration levelEnum = OrganisationLevelEnumeration.withName(dto.levelName());
        OrganisationLevelEnumeration levelEnum = OrganisationLevelEnumeration.apply(dto.level());
        return new OrganisationEntity(
                dto.id().orElse(null),
                dto.name(),
                accountId,
                dto.parentId().orElse(null),
                levelEnum, null
        );
    }


    // Replace organisationHierarchyToVo and buildTreeforHirarichy with the following:

    public static List<OrganisationVO> organisationHierarchyToVo(List<OrganisationEntity[]> tree) {
        Set<OrganisationEntity> allEntities = tree.stream()
                .flatMap(arr -> Arrays.stream(arr).filter(Objects::nonNull))
                .collect(Collectors.toSet());

        Map<Optional<Long>, List<OrganisationEntity>> byParentId = allEntities.stream()
                .collect(Collectors.groupingBy(e -> Optional.ofNullable(e.getParentId())));

        // Roots: entities with parentId == null
        return byParentId.getOrDefault(Optional.<Long>empty(), List.of()).stream()
                .map(entity -> buildTreeWithEntityLevel(entity, byParentId))
                .sorted(Comparator.comparing(OrganisationVO::name))
                .toList();
    }

    private static OrganisationVO buildTreeWithEntityLevel(
            OrganisationEntity entity,
            Map<Optional<Long>, List<OrganisationEntity>> byParentId
    ) {
        List<OrganisationVO> children = byParentId
                .getOrDefault(Optional.ofNullable(entity.getId()), List.of())
                .stream()
                .map(child -> buildTreeWithEntityLevel(child, byParentId))
                .sorted(Comparator.comparing(OrganisationVO::name))
                .toList();

        return new OrganisationVO(
                Optional.ofNullable(entity.getId()),
                entity.getName(),
                entity.getOrgLevel(),
                Optional.empty(),
                children
        );
    }


    public static Optional<OrganisationVO> organisationSubtreeById(Long id, List<OrganisationEntity[]> tree) {
        Set<OrganisationEntity> allEntities = tree.stream()
                .flatMap(arr -> Arrays.stream(arr).filter(Objects::nonNull))
                .collect(Collectors.toSet());

        Map<Optional<Long>, List<OrganisationEntity>> byParentId = allEntities.stream()
                .collect(Collectors.groupingBy(e -> Optional.ofNullable(e.getParentId())));

        return allEntities.stream()
                .filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .map(root -> buildTreeWithEntityLevel(root, byParentId));
    }


}
