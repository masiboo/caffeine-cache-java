package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.BlacklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BlacklistJpaRepository extends JpaRepository<BlacklistEntity, Long> {

    @Query("""
    SELECT b FROM BlacklistEntity b 
    WHERE b.account.id = :accountId 
    AND (b.endDate IS NULL OR b.endDate > :now)
    """)
    List<BlacklistEntity> findActiveByAccount(@Param("accountId") Long accountId, @Param("now") LocalDateTime now);

    @Query("""
    SELECT b FROM BlacklistEntity b 
    WHERE b.account.id = :accountId 
    AND b.functionality = :functionality 
    AND (b.endDate IS NULL OR b.endDate > :now)
    """)
    List<BlacklistEntity> findByAccountIdAndFunctionalityAndActive(@Param("accountId") Long accountId, @Param("functionality") String functionality, @Param("now") LocalDateTime now);
}
