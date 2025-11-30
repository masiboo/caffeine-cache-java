package nl.ing.api.contacting.conf.domain.model.connection;

import nl.ing.api.contacting.conf.domain.entity.ActiveConnectionEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionDetailsEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionEntity;

public record ConnectionDetailsDTO(
        ConnectionDetailsEntity details,
        ConnectionEntity connection,
        ActiveConnectionEntity active
) {}
