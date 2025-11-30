package nl.ing.api.contacting.conf.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Entity
@Table(name = "ATTRIBUTES")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ENTITY")
    private String entity;

    @Column(name = "ENTITY_VALUE")
    private String entityValue;

    private String label;

    @Column(name = "LABEL_VALUE")
    private String labelValue;

    @Column(name = "LABEL_CONTENT", nullable = true)
    private String labelContent;

    @Column(name = "DISPLAY_ORDER", nullable = true)
    private Integer displayOrder;

    @Column(name = "ACCOUNT_ID")
    private Long accountId;

    public Optional<Long> getIdOptional() {
        return Optional.ofNullable(id);
    }

    public Optional<String> getLabelContentOptional() {
        return Optional.ofNullable(labelContent);
    }

    public Optional<Integer> getDisplayOrderOptional() {
        return Optional.ofNullable(displayOrder);
    }
}