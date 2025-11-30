package nl.ing.api.contacting.conf.business.kafka.entityevents;

import nl.ing.api.contacting.conf.entityevents.ContactingEntityKafkaEventJava;
import nl.ing.api.contacting.conf.entityevents.ContactingEntityTypeJava;
import nl.ing.api.contacting.conf.entityevents.ContactingEventTypeJava;
import nl.ing.api.contacting.java.domain.OrganisationVO;
import java.util.Optional;

public interface ContactingEntityKafkaEventDataJava<T> {
    ContactingEntityKafkaEventJava eventDeleteData(T t, Long accountId, Optional<String> modifiedBy);

    static ContactingEntityKafkaEventDataJava<OrganisationVO> organisationData() {
        return (org, accountId, modifiedBy) -> new ContactingEntityKafkaEventJava(
            ContactingEntityTypeJava.ORGANISATION,
            ContactingEventTypeJava.DELETED,
            org.id() != null ? org.id().get() : -1L,
            org.name(),
            accountId,
            modifiedBy
        );
    }
}

