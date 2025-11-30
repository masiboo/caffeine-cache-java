package nl.ing.api.contacting.conf.mapper;

import lombok.NoArgsConstructor;
import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataEntity;
import nl.ing.api.contacting.conf.domain.entity.SettingsMetadataOptionsEntity;
import nl.ing.api.contacting.conf.domain.model.accountsetting.AccountSettingConsumers;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingCapability;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataWithOptions;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsOptionsVO;
import nl.ing.api.contacting.conf.resource.dto.SettingsMetadataJavaDTO;
import nl.ing.api.contacting.conf.resource.dto.SettingsOptionsJavaDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SettingsMetadataMapperJava {

    public static List<SettingsMetadataVO> metadataToVO(List<SettingsMetadataWithOptions> settings) {
        Map<SettingsMetadataEntity, List<SettingsOptionsVO>> metadataToOptions = settings.stream()
                .collect(Collectors.groupingBy(
                        SettingsMetadataWithOptions::metadata,
                        Collectors.mapping(
                                entry -> Optional.ofNullable(entry.option())
                                        .map(SettingsMetadataMapperJava::optionToVO)
                                        .orElse(null),
                                Collectors.filtering(
                                        Objects::nonNull,
                                        Collectors.toList()
                                )
                        )
                ));

        return metadataToOptions.entrySet().stream()
                .map(entry -> createMetadataVO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(SettingsMetadataVO::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public static SettingsMetadataJavaDTO toSettingsMetadataJavaDTO(SettingsMetadataVO settingsMetadataVO) {
        return new SettingsMetadataJavaDTO(settingsMetadataVO.name(), settingsMetadataVO.inputType(), settingsMetadataVO.regex(),
                settingsMetadataVO.options().stream()
                        .map(SettingsMetadataMapperJava::toSettingsOptionsDTO).toList(),
                settingsMetadataVO.capability().stream().map(SettingCapability::value).toList(),
                settingsMetadataVO.consumers().stream().map(AccountSettingConsumers::value).toList());
    }

    private static SettingsMetadataVO createMetadataVO(SettingsMetadataEntity metadata, List<SettingsOptionsVO> options) {
        List<AccountSettingConsumers> consumers = Optional.ofNullable(metadata.getConsumers())
                .filter(str -> !str.isBlank())
                .map(consumersStr -> Arrays.stream(consumersStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(AccountSettingConsumers::fromValue)
                        .toList())
                .orElseGet(List::of);

        List<SettingCapability> capabilities = Optional.ofNullable(metadata.getCapability())
                .filter(str -> !str.isBlank())
                .map(capability -> List.of(SettingCapability.fromValue(capability)))
                .orElseGet(List::of);

        List<SettingsOptionsVO> sortedOptions = Optional.ofNullable(options)
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SettingsOptionsVO::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new SettingsMetadataVO(
                Optional.ofNullable(metadata.getId()),
                metadata.getName(),
                metadata.getInputType(),
                Optional.ofNullable(metadata.getRegex()),
                sortedOptions,
                capabilities,
                consumers
        );
    }

    private static SettingsOptionsJavaDTO toSettingsOptionsDTO(SettingsOptionsVO settingsOptionsVO) {
        return new SettingsOptionsJavaDTO(settingsOptionsVO.value(), settingsOptionsVO.displayName());
    }

    private static SettingsOptionsVO optionToVO(SettingsMetadataOptionsEntity option) {
        return new SettingsOptionsVO(
                Optional.ofNullable(option.getId()),
                option.getValue(),
                option.getDisplayName()
        );
    }
}
