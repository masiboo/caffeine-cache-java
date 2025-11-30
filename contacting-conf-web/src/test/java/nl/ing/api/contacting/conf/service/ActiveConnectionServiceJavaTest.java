package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.resource.connection.ActivateDto;
import com.ing.api.contacting.dto.java.resource.connection.ActiveConnectionsDto;
import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import com.ing.api.contacting.dto.java.resource.connection.WebhookDto;
import nl.ing.api.contacting.conf.domain.entity.ActiveConnectionEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionDetailsEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionEntity;
import nl.ing.api.contacting.conf.repository.ActiveConnectionJpaRepository;
import nl.ing.api.contacting.conf.repository.ConnectionDetailsCacheRepository;
import nl.ing.api.contacting.conf.repository.ConnectionDetailsJpaRepository;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ActiveConnectionServiceJavaTest {

    @Mock
    ActiveConnectionJpaRepository activeConnectionJpaRepository;

    @Mock
    ConnectionDetailsCacheRepository connectionDetailsCacheRepository;

    @Mock
    ConnectionDetailsJpaRepository connectionDetailsJpaRepository;

    @Mock
    WebhookConnectionService webhookConnectionService;

    @Mock
    ContactingCache contactingCache;

    @InjectMocks
    ActiveConnectionServiceJava activeConnectionServiceJava;

    @Test
    @DisplayName("saveOrUpdate updates existing connections and activates webhooks")
    void saveOrUpdate_positive() {
        var connectionDto = new ActiveConnectionsDto(1L, 101L);
        var webhookDto = mock(WebhookDto.class);
        var activateDto = new ActivateDto(List.of(connectionDto), List.of(webhookDto));

        var connectionEntity = mock(ConnectionEntity.class);
        var connectionDetailsEntity = mock(ConnectionDetailsEntity.class);
        when(connectionEntity.getAccountId()).thenReturn(123L);

        var entity = ActiveConnectionEntity.builder()
                .connectionId(connectionEntity.getId())
                .connectionDetails(connectionDetailsEntity)
                .connection(connectionEntity)
                .build();

        when(activeConnectionJpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(connectionDetailsJpaRepository.findById(101L)).thenReturn(Optional.of(connectionDetailsEntity));
        when(activeConnectionJpaRepository.save(any())).thenReturn(entity);

        doNothing().when(webhookConnectionService).onlyOneActiveWebhook(anyList());
        doNothing().when(webhookConnectionService).activate(anyList());

        assertDoesNotThrow(() -> activeConnectionServiceJava.saveOrUpdate(activateDto));

        verify(activeConnectionJpaRepository).save(entity);
        verify(webhookConnectionService).activate(anyList());
    }

    @Test
    @DisplayName("saveOrUpdate handles empty connections")
    void saveOrUpdate_emptyConnections() {
        var activateDto = new ActivateDto(List.of(), List.of());
        doNothing().when(webhookConnectionService).onlyOneActiveWebhook(anyList());
        doNothing().when(webhookConnectionService).activate(anyList());

        assertDoesNotThrow(() -> activeConnectionServiceJava.saveOrUpdate(activateDto));

        verify(webhookConnectionService).activate(anyList());
        verify(activeConnectionJpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveOrUpdate logs and skips missing connection")
    void saveOrUpdate_missingConnection() {
        var connectionDto = new ActiveConnectionsDto(2L, 202L);

        WebhookDto dto1 = new WebhookDto(1L, ConnectionType.PRIMARY, "http://test1", 100L, 1);
        WebhookDto dto2 = new WebhookDto(2L, ConnectionType.PRIMARY, "http://test2", 101L, 0);
        List<WebhookDto> webhookDtos = List.of(dto1, dto2);
        var activateDto = new ActivateDto(List.of(connectionDto), webhookDtos);
        when(activeConnectionJpaRepository.findById(2L)).thenReturn(Optional.empty());
        doNothing().when(webhookConnectionService).onlyOneActiveWebhook(anyList());
        doNothing().when(webhookConnectionService).activate(anyList());

        assertDoesNotThrow(() -> activeConnectionServiceJava.saveOrUpdate(activateDto));

        verify(activeConnectionJpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveOrUpdate throws on repository error")
    void saveOrUpdate_repositoryError() {
        var connectionDto = new ActiveConnectionsDto(1L, 101L);
        var activateDto = new ActivateDto(List.of(connectionDto), List.of());

        when(activeConnectionJpaRepository.findById(1L)).thenThrow(new RuntimeException("DB error"));
        doNothing().when(webhookConnectionService).onlyOneActiveWebhook(anyList());

        var ex = assertThrows(RuntimeException.class, () -> activeConnectionServiceJava.saveOrUpdate(activateDto));
        assertTrue(ex.getMessage().contains("DB error"));
    }
}
