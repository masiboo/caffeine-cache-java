package nl.ing.api.contacting.conf.domain.entity.cassandra;

import lombok.*;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PrimaryKeyClass
public class SurveyCallRecordKey implements Serializable {

    @PrimaryKeyColumn(name = "account_friendly_name", type = PrimaryKeyType.PARTITIONED)
    private String accountFriendlyName;

    @PrimaryKeyColumn(name = "phone_num", type = PrimaryKeyType.PARTITIONED)
    private String phoneNum;

    @PrimaryKeyColumn(name = "offered_datetime", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private LocalDateTime offeredDatetime;
}

