package nl.ing.api.contacting.conf.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.InputTypeJava;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataVO;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.SettingsMetadataWithOptions;
import nl.ing.api.contacting.conf.mapper.SettingsMetadataMapperJava;
import nl.ing.api.contacting.conf.repository.SettingsMetadataOptionsJpaRepository;
import nl.ing.api.contacting.conf.resource.dto.SettingsMetadataJavaDTOs;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsMetadataServiceJava {
    private final SettingsMetadataOptionsJpaRepository settingsMetadataRepository;

    public SettingsMetadataJavaDTOs getSettingsMetadata() {
        List<SettingsMetadataWithOptions> metadataEntities = settingsMetadataRepository.findAllWithMetadata();
        List<SettingsMetadataVO> metadataVos = SettingsMetadataMapperJava.metadataToVO(metadataEntities);
        return new SettingsMetadataJavaDTOs(
                metadataVos.stream()
                        .map(SettingsMetadataMapperJava::toSettingsMetadataJavaDTO).toList());
    }

    @Transactional(readOnly = true)
    public Optional<SettingsMetadataVO> findByName(String name) {
        List<SettingsMetadataWithOptions> metadataList = settingsMetadataRepository.findByMetadataName(name);
        List<SettingsMetadataVO> voList = SettingsMetadataMapperJava.metadataToVO(metadataList);
        return voList.stream().findFirst();
    }

    public static boolean regexDontMatch(SettingsMetadataVO meta, String value) {
        return meta.regex()
                .map(regex -> !Pattern.compile(regex).matcher(value).matches())
                .orElse(false);
    }

    public static boolean optionsDontMatch(SettingsMetadataVO meta, String value) {
        return InputTypeJava.isValidatable(meta.inputType()) &&
                meta.options().stream()
                        .noneMatch(option -> option.value().equals(value));
    }
}
