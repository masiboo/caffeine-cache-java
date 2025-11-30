package nl.ing.api.contacting.conf.business.kafka.entityevents;

import nl.ing.api.contacting.conf.entityevents.ContactingEntityKafkaEventJava;
import nl.ing.api.contacting.java.domain.OrganisationVO;


import java.util.Optional;

public class ContactingEntityKafkaEventDataSyntaxJava {
    public static ContactingEntityKafkaEventJava toEventDeleteData(
            OrganisationVO org,
            Long accountId,
            Optional<String> modifiedBy) {
        return ContactingEntityKafkaEventDataJava.organisationData().eventDeleteData(org, accountId, modifiedBy);
    }
}




