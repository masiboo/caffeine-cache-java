package nl.ing.api.contacting.conf.domain.model.admintool;

import java.util.List;

/**
 * DTO for customer account settings
 */
public record CustomerAccountSettingsDTO(List<AccountSettingDTO> settings) {}

