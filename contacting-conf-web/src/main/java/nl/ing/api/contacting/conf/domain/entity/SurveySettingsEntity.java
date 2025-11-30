package nl.ing.api.contacting.conf.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SURVEY_SETTINGS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveySettingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "CHANNEL", nullable = false)
    private String channel;

    @Column(name = "CHANNEL_DIRECTION", nullable = false)
    private String channelDirection;

    @Column(name = "VOICE_SURVEY_ID", nullable = false)
    private String voiceSurveyId;

    @Column(name = "CALLFLOW_NAME")
    private String callflowName;

    @Column(name = "MIN_FREQUENCY")
    private Integer minFrequency;

    @Column(name = "DELAY")
    private Long delay;

    @Column(name = "SURVEY_OFFER_RATIO")
    private Float surveyOfferRatio;

    @Column(name = "MIN_CONTACT_LENGTH")
    private Long minContactLength;

    @Column(name = "SURVEY_FOR_TRANSFERS", nullable = false)
    private Boolean surveyForTransfers;

    @Column(name = "ACCOUNT_ID", nullable = false)
    private Long accountId;
}
