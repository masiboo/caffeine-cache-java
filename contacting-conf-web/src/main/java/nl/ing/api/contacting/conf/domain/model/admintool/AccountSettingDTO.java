package nl.ing.api.contacting.conf.domain.model.admintool;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Optional;

/**
 * DTO for account setting (moved from AdminToolElements)
 */
public record AccountSettingDTO(
        @JsonDeserialize(contentAs = Long.class) Optional<Long> id,
        String key,
        String value,
        Optional<List<String>> capability,
        Optional<List<String>> consumers,
        Long accountId
) {}

