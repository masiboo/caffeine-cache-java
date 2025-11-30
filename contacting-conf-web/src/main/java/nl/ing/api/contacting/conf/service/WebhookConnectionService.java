package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.resource.connection.WebhookDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.entity.WebhookConnectionEntity;
import nl.ing.api.contacting.conf.domain.model.connection.WebhookConnectionVO;
import nl.ing.api.contacting.conf.domain.model.webhook.UpdateSetting;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.ConnectionMapperJava;
import nl.ing.api.contacting.conf.mapper.WebhookConnectionMapper;
import nl.ing.api.contacting.conf.repository.AccountSettingsAuditRepository;
import nl.ing.api.contacting.conf.repository.AccountSettingsJpaRepository;
import nl.ing.api.contacting.conf.repository.WebhookConnectionsJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for managing webhook connections in the Contacting Configuration API.
 *
 * @author Auto-generated
 * @since 2025-10-10
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WebhookConnectionService {

    private final WebhookConnectionsJpaRepository webhookConnectionsRepository;
    private final AccountSettingsJpaRepository accountSettingsRepository;
    private final AccountSettingsAuditRepository accountSettingsAuditRepository;

    private final String accountSettingKey = "TWILIO_CALLBACK_BASE_URL";

    /**
     * Retrieves all webhook connections synchronously.
     *
     * @return a list of all WebhookConnectionVO
     */
    @Transactional(readOnly = true)
    public List<WebhookConnectionVO> getAll() {
        return webhookConnectionsRepository.findAll().stream()
                .map(WebhookConnectionMapper::toVO).toList();
    }

    /**
     * Retrieves all webhook connections for a given account synchronously.
     *
     * @param accountId the account ID to filter webhook connections
     * @return a list of WebhookConnectionVO for the account
     */
    @Transactional(readOnly = true)
    public List<WebhookConnectionVO> getAllByAccountId(Long accountId) {
        return webhookConnectionsRepository.findByAccountId(accountId).stream()
                .map(WebhookConnectionMapper::toVO).toList();
    }

    public void onlyOneActiveWebhook(List<WebhookDto> dtos) {
        // Group webhooks by accountId
        Map<Long, List<WebhookDto>> webhooksByAccount = dtos.stream()
                .collect(Collectors.groupingBy(WebhookDto::accountId));
        // For each account, check that exactly one webhook is active
        boolean isValid = webhooksByAccount.values().stream()
                .allMatch(webhooks -> webhooks.stream()
                        .filter(w -> w.isActive() != null && w.isActive() == 1)
                        .count() == 1);
        if (!isValid) {
            throw Errors.unexpected("Only one webhook can be active");
        }
    }

    /**
     * Activates a list of webhook configurations by updating account settings and saving webhook entities.
     *
     * @param webhookDtos List of {@link WebhookDto} to activate.
     * @return List of activated {@link WebhookDto}.
     */
    public void activate(List<WebhookDto> webhookDtos) {
        if (webhookDtos == null || webhookDtos.isEmpty()) {
            log.warn("No webhooks to activate (empty webhookDtos list)");
            return;
        }
        List<UpdateSetting> accountSettingUpdates = webhookDtos.stream()
                .filter(vo -> vo.isActive() != null && vo.isActive() != 0)
                .map(vo -> new UpdateSetting(accountSettingKey, vo.url(), vo.accountId()))
                .toList();

        updateSettings(accountSettingUpdates);
        Set<Long> ids = webhookDtos.stream()
                .map(dto -> dto.id() != null ? dto.id() : -1L)
                .collect(Collectors.toSet());
        webhookConnectionsRepository.activateConnections(ids);
        webhookConnectionsRepository.deactivateConnections(ids);
    }

    /**
     * Updates account settings based on a list of update instructions.
     *
     * @param entries List of {@link UpdateSetting} containing key, value, and accountId to update.
     */
    public void updateSettings(List<UpdateSetting> entries) {
        if (entries == null || entries.isEmpty()) {
            log.warn("No account settings to update (empty entries list)");
            return;
        }

        List<AccountSettingsEntity> updates = entries.stream()
                .flatMap(entry -> {
                    List<AccountSettingsEntity> settings = accountSettingsRepository.findByAccountIdAndKey(entry.accountId(), entry.key());
                    if (settings.isEmpty()) {
                        return Stream.empty();
                    }
                    return settings.stream()
                            .filter(setting -> !Objects.equals(setting.getValue(), entry.value()))
                            .peek(setting -> {
                                setting.setValue(entry.value());
                            });
                })
                .toList();

        if (!updates.isEmpty()) {
            accountSettingsRepository.saveAll(updates);
        } else {
            log.warn("No AccountSettingsEntity records updated for the given entries");
        }
    }

    @Transactional
    public List<WebhookConnectionVO> updateAll(List<WebhookConnectionVO> list) {
        List<UpdateSetting> accountSettingUpdate = list.stream()
                .filter(WebhookConnectionVO::active)
                .map(vo -> new UpdateSetting(accountSettingKey, vo.url(), vo.accountId()))
                .toList();
        accountSettingsAuditRepository.updateAll(accountSettingUpdate);
        List<WebhookConnectionEntity> updatedWebhookConnections = webhookConnectionsRepository.saveAll(ConnectionMapperJava.webhookConnectionsToEntities(list));
        return toDto(updatedWebhookConnections);
    }

    private List<WebhookConnectionVO> toDto(List<WebhookConnectionEntity> entities) {
        return entities == null ? List.of() : entities.stream().map(WebhookConnectionMapper::toVO).toList();
    }

}
