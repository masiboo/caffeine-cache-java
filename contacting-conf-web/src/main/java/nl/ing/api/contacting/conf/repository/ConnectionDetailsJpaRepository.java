package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.ConnectionDetailsEntity;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectionDetailsJpaRepository extends JpaRepository<ConnectionDetailsEntity, Long> {

    /**
     * Retrieves all joined data from ConnectionDetails, Connection, and ActiveConnection tables.
     *
     * @return a list of joined data as ConnectionDetailsDTOJava
     */
    @Query("""
                SELECT new nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsDTO(details, connection, active)
                FROM ConnectionDetailsEntity details
                JOIN details.connection connection
                JOIN ActiveConnectionEntity active ON active.connection.id = connection.id
            """)
    List<ConnectionDetailsDTO> findAllData();

    /**
     * Retrieves all joined data from ConnectionDetails, Connection, and ActiveConnection tables.
     *
     * @param accountId the account ID to filter connections
     * @return a list of joined data as ConnectionDetailsDTO
     */
    @Query("""
            SELECT new nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsDTO(details, connection, active)
            FROM ConnectionDetailsEntity details
            JOIN details.connection connection
            JOIN ActiveConnectionEntity active ON active.connection.id = connection.id
            WHERE connection.accountId = :accountId
            """)
    List<ConnectionDetailsDTO> findAllDataByAccountId(@Param("accountId") Long accountId);

    /**
     * Retrieves all joined data from ConnectionDetails, Connection, and ActiveConnection tables.
     *
     * @param accountId the account ID to filter connections
     * @return a list of joined data as ConnectionDetailsDTO
     */
    @Query("""
            SELECT new nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsDTO(details, connection, active)
            FROM ConnectionDetailsEntity details
            JOIN details.connection connection
            JOIN ActiveConnectionEntity active ON active.connection.id = connection.id
            WHERE connection.accountId = :accountId and connection.layer = :layer
            """)
    List<ConnectionDetailsDTO> getByAccountAndLayer(@Param("accountId") Long accountId, @Param("layer") String layer);

}
