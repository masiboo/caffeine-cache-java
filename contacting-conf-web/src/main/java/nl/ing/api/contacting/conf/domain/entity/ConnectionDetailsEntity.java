package nl.ing.api.contacting.conf.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "CONNECTIONS_DETAILS")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONNECTION_ID", nullable = false)
    private ConnectionEntity connection;

    @Column(name = "CONNECTION_TYPE", nullable = false)
    private String connectionType;

    @Column(name = "EDGE_LOCATION", nullable = false)
    private String edgeLocation;

    @Column(name = "URL", nullable = false)
    private String url;

    @Column(name = "REGION")
    private String region;
}

