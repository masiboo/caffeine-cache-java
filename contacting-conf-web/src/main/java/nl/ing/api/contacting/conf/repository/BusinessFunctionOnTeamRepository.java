package nl.ing.api.contacting.conf.repository;

import nl.ing.api.contacting.conf.domain.entity.cassandra.BusinessFunctionOnTeamEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessFunctionOnTeamRepository extends CassandraRepository<BusinessFunctionOnTeamEntity, String> {

    List<BusinessFunctionOnTeamEntity> findByAccountFriendlyName(String accountFriendlyName);

    void deleteByAccountFriendlyNameAndBusinessFunctionAndOrganisationIdAndRole(
            String accountFriendlyName,
            String businessFunction,
            int organisationId,
            String role
    );

}