package nl.ing.api.contacting.conf.domain.entity.cassandra;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("survey_call_records")
public class SurveyCallRecordEntity {

    @PrimaryKey
    private SurveyCallRecordKey key;

    @Column("survey_name")
    private String surveyName;
}

