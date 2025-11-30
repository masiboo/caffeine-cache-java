package nl.ing.api.contacting.conf.resource.dto;

import nl.ing.api.contacting.conf.domain.InputTypeJava;

import java.util.List;
import java.util.Optional;

public record SettingsMetadataJavaDTO(String name, InputTypeJava inputType, Optional<String> regex,
                                      List<SettingsOptionsJavaDTO> options, List<String> capability,
                                      List<String> consumers) {
}
