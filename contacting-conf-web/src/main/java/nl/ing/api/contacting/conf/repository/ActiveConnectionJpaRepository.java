package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.ActiveConnectionEntity;
import nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Provides CRUD operations for ActiveConnections table using async actions.
 */
@Repository
public interface ActiveConnectionJpaRepository extends JpaRepository<ActiveConnectionEntity, Long> {

    @Query("""
        SELECT new nl.ing.api.contacting.conf.domain.model.connection.ConnectionDetailsDTO(cd, c, ac)
        FROM ActiveConnectionEntity ac
        JOIN ac.connection c
        JOIN ac.connectionDetails cd
        """)
    List<ConnectionDetailsDTO> getConnectionWithDetails();

}
