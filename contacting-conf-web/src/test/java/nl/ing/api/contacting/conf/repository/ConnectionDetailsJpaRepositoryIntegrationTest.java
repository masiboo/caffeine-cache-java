package nl.ing.api.contacting.conf.repository;

import com.ing.api.contacting.dto.java.resource.connection.Layer;
import nl.ing.api.contacting.conf.domain.entity.ActiveConnectionEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionDetailsEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionEntity;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ConnectionDetailsJpaRepository Integration Tests")
class ConnectionDetailsJpaRepositoryIntegrationTest {

    @Autowired
    private ConnectionDetailsJpaRepository connectionDetailsRepository;

    @Autowired
    private ActiveConnectionJpaRepository activeConnectionJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("should find all data with complete details")
    void shouldFindAllDataWithCompleteDetails() {
        // Create and persist connection first
        ConnectionEntity connection = ConnectionEntity.builder()
                .accountId(100L)
                .layer(Layer.FRONTEND.getValue())
                .domain("test.domain.com")
                .build();
        ConnectionEntity savedConnection = entityManager.persistAndFlush(connection);

        // Create and persist connection details
        ConnectionDetailsEntity details = ConnectionDetailsEntity.builder()
                .connection(savedConnection)
                .connectionType("REST")
                .edgeLocation("US-EAST")
                .url("https://api.example.com")
                .region("us-east-1")
                .build();
        ConnectionDetailsEntity savedDetails = entityManager.persistAndFlush(details);

        // Create active connection with proper ID setting
        ActiveConnectionEntity active = ActiveConnectionEntity.builder()
                .connectionId(savedConnection.getId())
                .connection(savedConnection)
                .connectionDetails(savedDetails)
                .build();
        entityManager.persistAndFlush(active);

        // Clear persistence context to ensure fresh data retrieval
        entityManager.clear();

        // Test findAllData
        List<ConnectionDetailsDTO> result = connectionDetailsRepository.findAllData();

        assertThat(result).hasSize(1);
        ConnectionDetailsDTO dto = result.get(0);
        assertThat(dto.details().getConnectionType()).isEqualTo("REST");
        assertThat(dto.connection().getAccountId()).isEqualTo(100L);
        assertThat(dto.connection().getLayer()).isEqualTo(Layer.FRONTEND.getValue());
    }

    @Test
    @DisplayName("should find data by account ID with details")
    void shouldFindDataByAccountIdWithDetails() {
        // Create test data for account 200
        ConnectionEntity connection1 = ConnectionEntity.builder()
                .accountId(200L)
                .layer(Layer.BACKEND.getValue())
                .domain("backend.domain.com")
                .build();
        ConnectionEntity savedConnection1 = entityManager.persistAndFlush(connection1);

        ConnectionDetailsEntity details1 = ConnectionDetailsEntity.builder()
                .connection(savedConnection1)
                .connectionType("SOAP")
                .edgeLocation("EU-WEST")
                .url("https://backend.example.com")
                .region("eu-west-1")
                .build();
        ConnectionDetailsEntity savedDetails1 = entityManager.persistAndFlush(details1);

        ActiveConnectionEntity active1 = ActiveConnectionEntity.builder()
                .connectionId(savedConnection1.getId())
                .connection(savedConnection1)
                .connectionDetails(savedDetails1)
                .build();
        entityManager.persistAndFlush(active1);

        // Create test data for different account (should not be returned)
        ConnectionEntity connection2 = ConnectionEntity.builder()
                .accountId(300L)
                .layer(Layer.FRONTEND.getValue())
                .domain("frontend.domain.com")
                .build();
        ConnectionEntity savedConnection2 = entityManager.persistAndFlush(connection2);

        ConnectionDetailsEntity details2 = ConnectionDetailsEntity.builder()
                .connection(savedConnection2)
                .connectionType("REST")
                .edgeLocation("US-WEST")
                .url("https://other.example.com")
                .region("us-west-1")
                .build();
        ConnectionDetailsEntity savedDetails2 = entityManager.persistAndFlush(details2);

        ActiveConnectionEntity active2 = ActiveConnectionEntity.builder()
                .connectionId(savedConnection2.getId())
                .connection(savedConnection2)
                .connectionDetails(savedDetails2)
                .build();
        entityManager.persistAndFlush(active2);

        // Test findAllDataByAccountId for account 200
        List<ConnectionDetailsDTO> result = connectionDetailsRepository.findAllDataByAccountId(200L);

        assertThat(result).hasSize(1);
        ConnectionDetailsDTO dto = result.get(0);
        assertThat(dto.details().getConnectionType()).isEqualTo("SOAP");
        assertThat(dto.connection().getAccountId()).isEqualTo(200L);
        assertThat(dto.connection().getLayer()).isEqualTo(Layer.BACKEND.getValue());
    }

    @Test
    @DisplayName("should get data by account and layer")
    void shouldGetDataByAccountAndLayer() {
        // Create multiple connections for same account but different layers
        ConnectionEntity frontendConnection = ConnectionEntity.builder()
                .accountId(400L)
                .layer(Layer.FRONTEND.getValue())
                .domain("frontend.domain.com")
                .build();
        ConnectionEntity savedFrontend = entityManager.persistAndFlush(frontendConnection);

        ConnectionEntity backendConnection = ConnectionEntity.builder()
                .accountId(400L)
                .layer(Layer.BACKEND.getValue())
                .domain("backend.domain.com")
                .build();
        ConnectionEntity savedBackend = entityManager.persistAndFlush(backendConnection);

        // Create details for both connections
        ConnectionDetailsEntity frontendDetails = ConnectionDetailsEntity.builder()
                .connection(savedFrontend)
                .connectionType("REST")
                .edgeLocation("US-CENTRAL")
                .url("https://frontend.example.com")
                .region("us-central-1")
                .build();
        ConnectionDetailsEntity savedFrontendDetails = entityManager.persistAndFlush(frontendDetails);

        ConnectionDetailsEntity backendDetails = ConnectionDetailsEntity.builder()
                .connection(savedBackend)
                .connectionType("gRPC")
                .edgeLocation("US-CENTRAL")
                .url("https://backend-grpc.example.com")
                .region("us-central-1")
                .build();
        ConnectionDetailsEntity savedBackendDetails = entityManager.persistAndFlush(backendDetails);

        // Create active connections for both
        ActiveConnectionEntity frontendActive = ActiveConnectionEntity.builder()
                .connectionId(savedFrontend.getId())
                .connection(savedFrontend)
                .connectionDetails(savedFrontendDetails)
                .build();
        entityManager.persistAndFlush(frontendActive);

        ActiveConnectionEntity backendActive = ActiveConnectionEntity.builder()
                .connectionId(savedBackend.getId())
                .connection(savedBackend)
                .connectionDetails(savedBackendDetails)
                .build();
        entityManager.persistAndFlush(backendActive);

        // Test getByAccountAndLayer for BACKEND only
        List<ConnectionDetailsDTO> result = connectionDetailsRepository.getByAccountAndLayer(400L, Layer.BACKEND.getValue());

        assertThat(result).hasSize(1);
        ConnectionDetailsDTO dto = result.get(0);
        assertThat(dto.details().getConnectionType()).isEqualTo("gRPC");
        assertThat(dto.connection().getAccountId()).isEqualTo(400L);
        assertThat(dto.connection().getLayer()).isEqualTo(Layer.BACKEND.getValue());
    }

    @Test
    @DisplayName("should return empty result for non-existing account ID")
    void shouldReturnEmptyResultForNonExistingAccountId() {
        List<ConnectionDetailsDTO> result = connectionDetailsRepository.findAllDataByAccountId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty result for non-existing account and layer combination")
    void shouldReturnEmptyResultForNonExistingAccountAndLayer() {
        List<ConnectionDetailsDTO> result = connectionDetailsRepository.getByAccountAndLayer(999L, Layer.BACKEND.getValue());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should handle multiple connections per account correctly")
    void shouldHandleMultipleConnectionsPerAccountCorrectly() {
        // Create multiple connections for same account
        ConnectionEntity connection1 = ConnectionEntity.builder()
                .accountId(500L)
                .layer(Layer.FRONTEND.getValue())
                .domain("api1.domain.com")
                .build();
        ConnectionEntity savedConnection1 = entityManager.persistAndFlush(connection1);

        ConnectionEntity connection2 = ConnectionEntity.builder()
                .accountId(500L)
                .layer(Layer.FRONTEND.getValue())
                .domain("api2.domain.com")
                .build();
        ConnectionEntity savedConnection2 = entityManager.persistAndFlush(connection2);

        // Create details for both connections
        ConnectionDetailsEntity details1 = ConnectionDetailsEntity.builder()
                .connection(savedConnection1)
                .connectionType("REST")
                .edgeLocation("US-EAST")
                .url("https://api1.example.com")
                .region("us-east-1")
                .build();
        ConnectionDetailsEntity savedDetails1 = entityManager.persistAndFlush(details1);

        ConnectionDetailsEntity details2 = ConnectionDetailsEntity.builder()
                .connection(savedConnection2)
                .connectionType("GraphQL")
                .edgeLocation("US-WEST")
                .url("https://api2.example.com")
                .region("us-west-1")
                .build();
        ConnectionDetailsEntity savedDetails2 = entityManager.persistAndFlush(details2);

        // Create active connections
        ActiveConnectionEntity active1 = ActiveConnectionEntity.builder()
                .connectionId(savedConnection1.getId())
                .connection(savedConnection1)
                .connectionDetails(savedDetails1)
                .build();
        entityManager.persistAndFlush(active1);

        ActiveConnectionEntity active2 = ActiveConnectionEntity.builder()
                .connectionId(savedConnection2.getId())
                .connection(savedConnection2)
                .connectionDetails(savedDetails2)
                .build();
        entityManager.persistAndFlush(active2);

        // Test that both connections are returned
        List<ConnectionDetailsDTO> result = connectionDetailsRepository.findAllDataByAccountId(500L);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(dto -> dto.details().getConnectionType())
                .containsExactlyInAnyOrder("REST", "GraphQL");
        assertThat(result)
                .allMatch(dto -> dto.connection().getAccountId().equals(500L));
    }

    @Test
    @DisplayName("should handle basic CRUD operations on connection details")
    void shouldHandleBasicCrudOperationsOnConnectionDetails() {
        // Create and save a connection
        ConnectionEntity connection = ConnectionEntity.builder()
                .accountId(600L)
                .layer(Layer.BACKEND.getValue())
                .domain("websocket.domain.com")
                .build();
        ConnectionEntity savedConnection = entityManager.persistAndFlush(connection);

        // Create and save connection details
        ConnectionDetailsEntity details = ConnectionDetailsEntity.builder()
                .connection(savedConnection)
                .connectionType("WebSocket")
                .edgeLocation("ASIA-PACIFIC")
                .url("wss://websocket.example.com")
                .region("ap-southeast-1")
                .build();
        ConnectionDetailsEntity savedDetails = entityManager.persistAndFlush(details);

        // Verify save operation
        assertThat(savedDetails.getId()).isNotNull();
        assertThat(savedDetails.getConnectionType()).isEqualTo("WebSocket");

        // Verify findById operation
        var foundDetails = connectionDetailsRepository.findById(savedDetails.getId());
        assertThat(foundDetails).isPresent();
        assertThat(foundDetails.get().getUrl()).isEqualTo("wss://websocket.example.com");

        // Verify update operation
        savedDetails.setUrl("wss://updated-websocket.example.com");
        ConnectionDetailsEntity updatedDetails = entityManager.persistAndFlush(savedDetails);
        assertThat(updatedDetails.getUrl()).isEqualTo("wss://updated-websocket.example.com");

        // Verify delete operation
        entityManager.remove(savedDetails);
        entityManager.flush();
        var deletedDetails = connectionDetailsRepository.findById(savedDetails.getId());
        assertThat(deletedDetails).isEmpty();
    }

    @Test
    @DisplayName("should maintain referential integrity between entities")
    void shouldMaintainReferentialIntegrityBetweenEntities() {
        // Create first connection
        ConnectionEntity connection1 = ConnectionEntity.builder()
                .accountId(700L)
                .layer(Layer.FRONTEND.getValue())
                .domain("primary.domain.com")
                .build();
        ConnectionEntity savedConnection1 = entityManager.persistAndFlush(connection1);

        // Create second connection
        ConnectionEntity connection2 = ConnectionEntity.builder()
                .accountId(700L)
                .layer(Layer.FRONTEND.getValue())
                .domain("backup.domain.com")
                .build();
        ConnectionEntity savedConnection2 = entityManager.persistAndFlush(connection2);

        // Create details for each connection
        ConnectionDetailsEntity details1 = ConnectionDetailsEntity.builder()
                .connection(savedConnection1)
                .connectionType("REST")
                .edgeLocation("PRIMARY")
                .url("https://primary.example.com")
                .region("primary-region")
                .build();

        ConnectionDetailsEntity details2 = ConnectionDetailsEntity.builder()
                .connection(savedConnection2)
                .connectionType("REST")
                .edgeLocation("BACKUP")
                .url("https://backup.example.com")
                .region("backup-region")
                .build();

        ConnectionDetailsEntity savedDetails1 = entityManager.persistAndFlush(details1);
        ConnectionDetailsEntity savedDetails2 = entityManager.persistAndFlush(details2);

        // Create active connections for both details
        ActiveConnectionEntity active1 = ActiveConnectionEntity.builder()
                .connectionId(savedConnection1.getId())
                .connection(savedConnection1)
                .connectionDetails(savedDetails1)
                .build();

        ActiveConnectionEntity active2 = ActiveConnectionEntity.builder()
                .connectionId(savedConnection2.getId())
                .connection(savedConnection2)
                .connectionDetails(savedDetails2)
                .build();

        entityManager.persistAndFlush(active1);
        entityManager.persistAndFlush(active2);

        // Verify both details are linked to their respective connections
        List<ConnectionDetailsDTO> result = connectionDetailsRepository.findAllDataByAccountId(700L);
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(dto -> dto.details().getEdgeLocation())
                .containsExactlyInAnyOrder("PRIMARY", "BACKUP");
        assertThat(result)
                .allMatch(dto -> dto.connection().getAccountId().equals(700L));
    }
}
