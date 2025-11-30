package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import com.ing.api.contacting.dto.java.resource.connection.WebhookDto;
import nl.ing.api.contacting.conf.domain.entity.AccountSettingsEntity;
import nl.ing.api.contacting.conf.domain.entity.WebhookConnectionEntity;
import nl.ing.api.contacting.conf.domain.model.connection.WebhookConnectionVO;
import nl.ing.api.contacting.conf.domain.model.webhook.UpdateSetting;
import nl.ing.api.contacting.conf.repository.AccountSettingsJpaRepository;
import nl.ing.api.contacting.conf.repository.WebhookConnectionsJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WebhookConnectionServiceJavaTest {

    @Mock
    private WebhookConnectionsJpaRepository webhookConnectionsRepository;

    @InjectMocks
    private WebhookConnectionService webhookConnectionService;

    @Mock
    private AccountSettingsJpaRepository accountSettingsRepository;

    @Test
    @DisplayName("testGetAll")
    void testGetAll() {
        WebhookConnectionEntity entity = WebhookConnectionEntity.builder()
                .id(1L)
                .connectionType(ConnectionType.PRIMARY.getValue())
                .url("https://webhook.test.com")
                .accountId(100L)
                .active(true)
                .build();

        when(webhookConnectionsRepository.findAll()).thenReturn(List.of(entity));

        List<WebhookConnectionVO> result = webhookConnectionService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        WebhookConnectionVO actual = result.get(0);

        // Assert all fields
        assertNotNull(actual.id());
        assertEquals(1L, actual.id());
        assertNotNull(actual.connectionType());
        assertEquals(ConnectionType.PRIMARY, actual.connectionType());
        assertNotNull(actual.url());
        assertEquals("https://webhook.test.com", actual.url());
        assertNotNull(actual.accountId());
        assertEquals(100L, actual.accountId());
        assertTrue(actual.active());

        verify(webhookConnectionsRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("testGetAllByAccountId")
    void testGetAllByAccountId() {
        Long accountId = 200L;
        WebhookConnectionEntity entity = WebhookConnectionEntity.builder()
                .id(2L)
                .connectionType(ConnectionType.FALLBACK.getValue())
                .url("https://webhook.account.com")
                .accountId(accountId)
                .active(true)
                .build();

        when(webhookConnectionsRepository.findByAccountId(accountId)).thenReturn(List.of(entity));

        List<WebhookConnectionVO> result = webhookConnectionService.getAllByAccountId(accountId);

        assertNotNull(result);
        assertEquals(1, result.size());
        WebhookConnectionVO actual = result.get(0);

        // Assert all fields
        assertNotNull(actual.id());
        assertEquals(2L, actual.id());
        assertNotNull(actual.connectionType());
        assertEquals(200L, actual.accountId());
        assertEquals(ConnectionType.FALLBACK, actual.connectionType());
        assertNotNull(actual.url());
        assertEquals("https://webhook.account.com", actual.url());
        assertNotNull(actual.accountId());
        assertEquals(accountId, actual.accountId());
        assertTrue(actual.active());

        verify(webhookConnectionsRepository, times(1)).findByAccountId(accountId);
    }

    @Test
    @DisplayName("testGetAllEmpty")
    void testGetAllEmpty() {
        when(webhookConnectionsRepository.findAll()).thenReturn(List.of());

        List<WebhookConnectionVO> result = webhookConnectionService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(webhookConnectionsRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("testGetAllByAccountIdEmpty")
    void testGetAllByAccountIdEmpty() {
        Long accountId = 300L;
        when(webhookConnectionsRepository.findByAccountId(accountId)).thenReturn(List.of());

        List<WebhookConnectionVO> result = webhookConnectionService.getAllByAccountId(accountId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(webhookConnectionsRepository, times(1)).findByAccountId(accountId);
    }

    @Test
    @DisplayName("testGetAllWithNullFields")
    void testGetAllWithNullFields() {
        WebhookConnectionEntity entity = WebhookConnectionEntity.builder()
                .id(null)
                .connectionType(null)
                .url(null)
                .accountId(null)
                .active(false)
                .build();

        when(webhookConnectionsRepository.findAll()).thenReturn(List.of(entity));

        List<WebhookConnectionVO> result = webhookConnectionService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        WebhookConnectionVO actual = result.get(0);

        // Assert all fields for null/false
        assertNull(actual.id());
        assertNull(actual.connectionType());
        assertNull(actual.url());
        assertNull(actual.accountId());
        assertFalse(actual.active());

        verify(webhookConnectionsRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("updateSettings: should do nothing when entries is null")
    void updateSettings_nullEntries() {
        webhookConnectionService.updateSettings(null);
        verifyNoInteractions(accountSettingsRepository);
    }

    @Test
    @DisplayName("updateSettings: should do nothing when entries is empty")
    void updateSettings_emptyEntries() {
        webhookConnectionService.updateSettings(List.of());
        verifyNoInteractions(accountSettingsRepository);
    }

    @Test
    @DisplayName("updateSettings: should do nothing when no settings found in DB")
    void updateSettings_noSettingsFound() {
        var entry = new UpdateSetting("KEY", "VAL", 1L);
        when(accountSettingsRepository.findByAccountIdAndKey(1L, "KEY")).thenReturn(List.of());
        webhookConnectionService.updateSettings(List.of(entry));
        verify(accountSettingsRepository).findByAccountIdAndKey(1L, "KEY");
        verify(accountSettingsRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("updateSettings: should not update when value is already up-to-date")
    void updateSettings_valueAlreadySet() {
        var entry = new UpdateSetting("KEY", "VAL", 1L);
        var entity = new AccountSettingsEntity();
        entity.setId(10L);
        entity.setValue("VAL");
        when(accountSettingsRepository.findByAccountIdAndKey(1L, "KEY")).thenReturn(List.of(entity));
        webhookConnectionService.updateSettings(List.of(entry));
        verify(accountSettingsRepository).findByAccountIdAndKey(1L, "KEY");
        verify(accountSettingsRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("updateSettings: should update and save when value is different")
    void updateSettings_valueNeedsUpdate() {
        var entry = new UpdateSetting("KEY", "NEW", 1L);
        var entity = new AccountSettingsEntity();
        entity.setId(10L);
        entity.setValue("OLD");
        when(accountSettingsRepository.findByAccountIdAndKey(1L, "KEY")).thenReturn(List.of(entity));
        webhookConnectionService.updateSettings(List.of(entry));
        verify(accountSettingsRepository).findByAccountIdAndKey(1L, "KEY");
        verify(accountSettingsRepository).saveAll(argThat(list -> {
            if (list instanceof List<?> l) {
                return l.size() == 1 && "NEW".equals(((AccountSettingsEntity) l.get(0)).getValue());
            }
            return false;
        }));
    }

    @Test
    @DisplayName("activate should update account settings and activate/deactivate webhooks")
    void activate_shouldUpdateAccountSettingsAndActivateDeactivateWebhooks() {
        WebhookDto activeWebhook = mock(WebhookDto.class);
        when(activeWebhook.isActive()).thenReturn(1);
        when(activeWebhook.url()).thenReturn("https://webhook.url");
        when(activeWebhook.accountId()).thenReturn(42L);
        when(activeWebhook.id()).thenReturn(100L);

        WebhookDto inactiveWebhook = mock(WebhookDto.class);
        when(inactiveWebhook.isActive()).thenReturn(0);
        when(inactiveWebhook.id()).thenReturn(101L);

        List<WebhookDto> webhookDtos = List.of(activeWebhook, inactiveWebhook);

        doNothing().when(webhookConnectionsRepository).activateConnections(anySet());
        doNothing().when(webhookConnectionsRepository).deactivateConnections(anySet());

        webhookConnectionService.activate(webhookDtos);

        verify(webhookConnectionsRepository, times(1)).activateConnections(Set.of(100L, 101L));
        verify(webhookConnectionsRepository, times(1)).deactivateConnections(Set.of(100L, 101L));

    }

    @Test
    @DisplayName("activate should log warning and do nothing for null or empty input")
    void activate_shouldLogWarningAndDoNothingForNullOrEmptyInput() {
        webhookConnectionService.activate(null);
        webhookConnectionService.activate(List.of());

        verifyNoInteractions(webhookConnectionsRepository);
        verifyNoInteractions(accountSettingsRepository);
    }
}
