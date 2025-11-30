package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.java.resource.connection.*;
import com.ing.api.contacting.dto.java.resource.connection.Layer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.ActiveConnectionEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionDetailsEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionEntity;
import nl.ing.api.contacting.conf.domain.model.connection.*;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsVO;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.ConnectionDetailsMapper;
import nl.ing.api.contacting.conf.mapper.ConnectionMapperJava;
import nl.ing.api.contacting.conf.mapper.ConnectionMapperJavaV1;
import nl.ing.api.contacting.conf.repository.ConnectionDetailsCacheRepository;
import nl.ing.api.contacting.conf.repository.ConnectionDetailsJpaRepository;
import nl.ing.api.contacting.conf.resource.dto.ConnectionModelV1;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing active connections in the Contacting Configuration API.
 *
 * @author Ajit Singh
 * @since 2025-10-10
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ConnectionDetailsServiceJava {

    private final UrlValidator urlValidator = new UrlValidator(new String[]{"wss", "https", "http"});

    private final ConnectionDetailsCacheRepository connectionDetailsCacheRepository;
    private final ConnectionDetailsJpaRepository connectionDetailsJpaRepository;
    private final WebhookConnectionService webhookConnectionService;
    private final IpAddressServiceJava ipAddressServiceJava;
    private final ActiveConnectionServiceJava activeConnectionService;

    @Transactional(readOnly = true)
    public ConnectionModel.Connection getAllConnectionsForAccount(ContactingContext contactingContext) {
        List<ConnectionWithDetails> connections = getAllByAccountId(contactingContext);
        List<WebhookConnectionVO> webhooks = webhookConnectionService.getAllByAccountId(contactingContext.accountId());
        return ConnectionMapperJava.toDTO(connections, webhooks, contactingContext.accountId());
    }

    @Transactional(readOnly = true)
    public ConnectionModelV1.AllConnections getAllConnections() {

        List<ConnectionWithDetails> connections = getAll();
        List<WebhookConnectionVO> webhooks = webhookConnectionService.getAll();

        return new ConnectionModelV1.AllConnections(ConnectionMapperJavaV1.toDTOV1(connections, webhooks));
    }

    @Transactional(readOnly = true)
    public ConnectionModel.BackendConnections getAllBackend(Long accountId) {

        List<ConnectionWithDetails> byAccountAndLayer = getByAccountAndLayer(accountId, Layer.BACKEND);

        List<ConnectionModel.TwilioDomain> dtos =
                ConnectionMapperJava.toDomainDTO(byAccountAndLayer, Layer.BACKEND);

        return new ConnectionModel.BackendConnections(dtos);
    }

    @Transactional(readOnly = true)
    public ConnectionModel.FrontendConnections getAllFrontEnd(Long accountId) {

        List<ConnectionWithDetails> byAccountAndLayer = getByAccountAndLayer(accountId, Layer.FRONTEND);

        List<ConnectionModel.TwilioDomain> dtos =
                ConnectionMapperJava.toDomainDTO(byAccountAndLayer, Layer.FRONTEND);

        return new ConnectionModel.FrontendConnections(dtos);
    }


    /**
     * Retrieves all active connections for a given account and layer.
     *
     * @param accountId the account ID to filter connections
     * @param layer     the connection layer to filter (e.g., Backend)
     * @return a list of ConnectionWithDetails
     */
    @Transactional(readOnly = true)
    public List<ConnectionWithDetails> getByAccountAndLayer(Long accountId, Layer layer) {
        List<ConnectionDetailsDTO> results = connectionDetailsJpaRepository.getByAccountAndLayer(accountId, layer.getValue());
        return results.stream()
                .map(this::toConnectionWithDetails)
                .toList();
    }

    /**
     * Retrieves all active connections.
     *
     * @return a list of all ConnectionWithDetails
     */
    @Transactional(readOnly = true)
    public List<ConnectionWithDetails> getAll() {
        List<ConnectionDetailsDTO> results = connectionDetailsJpaRepository.findAllData();
        return results.stream()
                .map(this::toConnectionWithDetails)
                .toList();
    }

    /**
     * Retrieves all active connections for a given account.
     * @param context the contacting context containing account information
     * @return a list of ConnectionWithDetails for the account
     */
    @Transactional(readOnly = true)
    public List<ConnectionWithDetails> getAllByAccountId(ContactingContext context) {
        List<ConnectionDetailsDTO> results = connectionDetailsCacheRepository.findAllForAccount(context);
        return results.stream()
                .map(this::toConnectionWithDetails)
                .toList();
    }

    /**
     * Maps a ConnectionDetailsDTO to a ConnectionWithDetails domain object.
     *
     * @param active the ConnectionDetailsDTO to map
     * @return the mapped ConnectionWithDetails
     */
    private ConnectionWithDetails toConnectionWithDetails(ConnectionDetailsDTO active) {
        ConnectionDetailsEntity details = active.details();
        ConnectionEntity connection = active.connection();
        ActiveConnectionEntity activeConnectionEntity = active.active();

        boolean isActive = activeConnectionEntity != null &&
                activeConnectionEntity.getConnectionDetails() != null &&
                details != null &&
                Objects.equals(details.getId(), activeConnectionEntity.getConnectionDetails().getId());

        ConnectionVO connectionVO = connection != null ?
                new ConnectionVO(
                        connection.getId(),
                        connection.getAccountId(),
                        Layer.fromValue(connection.getLayer()),
                        connection.getDomain()
                ) : null;

        ConnectionDetailsVO connectionDetailsVO = null;
        if (details != null) {
            connectionDetailsVO = new ConnectionDetailsVO(
                    details.getId(),
                    connection != null ? connection.getId() : null,
                    ConnectionType.fromValue(details.getConnectionType()),
                    details.getEdgeLocation(),
                    details.getUrl(),
                    details.getRegion()
            );
        }

        return new ConnectionWithDetails(connectionVO, connectionDetailsVO, isActive);
    }

    /**
     * Asynchronously retrieves agent connection settings for the given context and IP addresses.
     *
     * @param contactingContext the context containing account and session information
     * @param ipAddresses       the IP addresses to check for WFH status (may be null)
     * @return a {@link CompletableFuture} that completes with the agent connection settings
     */
    public AgentConnectionSettings getAgentConnectionSettings(ContactingContext contactingContext, String ipAddresses) {
        log.debug("Fetching agent connection settings for accountId={}, ipAddresses={}", contactingContext.accountId(), ipAddresses);
        Boolean wfhIndicator = ipAddressServiceJava.fetchWFHIndicator(ipAddresses, contactingContext);
        List<ConnectionWithDetails> frontendConnections = getByAccountAndLayer(contactingContext.accountId(), Layer.FRONTEND);
        ConnectionModelV1.FrontendConnections frontendConnectionsDto = new ConnectionModelV1.FrontendConnections(
                ConnectionMapperJavaV1.toDomainDTO(frontendConnections, Layer.FRONTEND));
        return new AgentConnectionSettings(wfhIndicator, frontendConnectionsDto);
    }

    public ConnectionModel.AllConnections getAllConnectionsV2() {
        List<ConnectionWithDetails> connections = activeConnectionService.getAll();
        List<WebhookConnectionVO> hooks = webhookConnectionService.getAll();
        return new ConnectionModel.AllConnections(ConnectionMapperJava.toDTO(connections, hooks));
    }

    public void createActivateConnections(ActivateDto activateDto) {
        activeConnectionService.saveOrUpdate(activateDto);
    }

    public List<WebhookConnectionVO> updateWebhookDetails(List<WebhookDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw Errors.badRequest("No webhook details provided for update");
        }

        // Validate all URLs in a single pass
        boolean hasInvalidUrl = dtos.stream().anyMatch(dto -> !urlValidator.isValid(dto.url()));
        if (hasInvalidUrl) {
            throw Errors.badRequest("Url specified is not valid");
        }

        // Ensure only one active webhook per account
        webhookConnectionService.onlyOneActiveWebhook(dtos);

        // Convert and update all webhooks
        return webhookConnectionService.updateAll(ConnectionMapperJava.toVO(dtos));
    }

    @Transactional
    public ConnectionDetailsVO updateConnectionDetails(ConnectionDetailsDtoJava connectionDetailsDto,
                                                       Long connectionId, Long detailId) {
        if (!urlValidator.isValid(connectionDetailsDto.url())) {
            throw Errors.badRequest("Url specified is not valid");
        }

        Long accountId = activeConnectionService.getAccountIdByConnectionId(connectionId);
        ConnectionDetailsVO detailsVO = ConnectionMapperJava.toVO(connectionDetailsDto);
        ConnectionDetailsVO updatedVO = ConnectionDetailsMapper.withDetails(detailsVO, detailId, connectionId);
        ConnectionDetailsEntity updatedEntity = update(accountId, updatedVO);
        return ConnectionDetailsMapper.toConnectionDetailsVO(updatedEntity);
    }

    public ConnectionDetailsEntity update(Long accountId, ConnectionDetailsVO detailsVO) {
        ConnectionDetailsEntity entity = ConnectionDetailsMapper.toEntity(detailsVO);
        return connectionDetailsCacheRepository.save(accountId, entity);
    }

}
