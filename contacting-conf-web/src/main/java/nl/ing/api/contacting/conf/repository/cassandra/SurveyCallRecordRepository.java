package nl.ing.api.contacting.conf.repository.cassandra;

import nl.ing.api.contacting.conf.domain.entity.cassandra.SurveyCallRecordEntity;
import nl.ing.api.contacting.conf.domain.entity.cassandra.SurveyCallRecordKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyCallRecordRepository extends CassandraRepository<SurveyCallRecordEntity, SurveyCallRecordKey> {

    List<SurveyCallRecordEntity> findByKeyAccountFriendlyNameAndKeyPhoneNum(
            String accountFriendlyName,
            String phoneNum
    );

}
