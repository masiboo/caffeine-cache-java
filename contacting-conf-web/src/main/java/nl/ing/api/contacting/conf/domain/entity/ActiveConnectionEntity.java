package nl.ing.api.contacting.conf.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ACTIVE_CONNECTIONS")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveConnectionEntity {

    @Id
    @Column(name = "CONNECTION_ID")
    private Long connectionId;

    @OneToOne
    @JoinColumn(name = "CONNECTION_ID", referencedColumnName = "ID")
    private ConnectionEntity connection;

    @OneToOne
    @JoinColumn(name = "CONNECTION_DETAILS_ID", referencedColumnName = "ID", nullable = false)
    private ConnectionDetailsEntity connectionDetails;
}
