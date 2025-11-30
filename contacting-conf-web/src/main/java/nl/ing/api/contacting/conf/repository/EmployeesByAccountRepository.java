package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.cassandra.EmployeesByAccountEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeesByAccountRepository extends CassandraRepository<EmployeesByAccountEntity, String> {

    Optional<EmployeesByAccountEntity> findByEmployeeIdAndAccountFriendlyName(String employeeId, String accountFriendlyName);
}