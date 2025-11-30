package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.resource.connection.ActivateDto;
import com.ing.api.contacting.dto.java.resource.connection.ActiveConnectionsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionWithDetails;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.ConnectionDetailsMapper;
import nl.ing.api.contacting.conf.repository.ActiveConnectionJpaRepository;
import nl.ing.api.contacting.conf.repository.ConnectionDetailsCacheRepository;
import nl.ing.api.contacting.conf.repository.ConnectionDetailsJpaRepository;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveConnectionServiceJava {
    private final ActiveConnectionJpaRepository activeConnectionJpaRepository;
    private final ConnectionDetailsCacheRepository connectionDetailsCacheRepository;
    private final ConnectionDetailsJpaRepository connectionDetailsJpaRepository;
    private final WebhookConnectionService webhookConnectionService;
    private final ContactingCache contactingCache;

    @Transactional
    public void saveOrUpdate(ActivateDto activateDto) {
        // Throw error if more than one active webhook found
        webhookConnectionService.onlyOneActiveWebhook(activateDto.webhooks());
        List<ActiveConnectionsDto> connections = activateDto.connections();
        if (connections == null || connections.isEmpty()) {
            log.warn("No ActiveConnectionsDto to update");
        } else {
            connections.forEach(dto ->
                    activeConnectionJpaRepository.findById(dto.connectionId())
                            .flatMap(existing ->
                                    connectionDetailsJpaRepository.findById(dto.connectionDetailsId())
                                            .map(details -> {
                                                existing.setConnectionDetails(details);
                                                activeConnectionJpaRepository.save(existing);
                                                log.debug("Connection ID {} found and updated", dto.connectionId());
                                                connectionDetailsCacheRepository.evictCacheAfterUpdate(existing.getConnection().getAccountId());
                                                return true;
                                            })
                            )
                            .orElseGet(() -> {
                                log.warn("Connection ID {} or ConnectionDetails ID {} not found, skipping update", dto.connectionId(), dto.connectionDetailsId());
                                return false;
                            })
            );
        }
        // Activate webhooks (side effect, result not used)
        webhookConnectionService.activate(activateDto.webhooks());
    }

    public List<ConnectionWithDetails> getAll() {
        return ConnectionDetailsMapper.toDto(activeConnectionJpaRepository
                .getConnectionWithDetails());

    }

    public Long getAccountIdByConnectionId(Long connectionId) {
        return activeConnectionJpaRepository.getConnectionWithDetails().stream()
                .filter(d -> d.details().getConnection().getId().equals(connectionId))
                .findFirst().map(s -> s.details().getConnection().getAccountId())
                .orElseThrow(()-> Errors.notFound("Connection with id " + connectionId + " not found."));

    }

}
