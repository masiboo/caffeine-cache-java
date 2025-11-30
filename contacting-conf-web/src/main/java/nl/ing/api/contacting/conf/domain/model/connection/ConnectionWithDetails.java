package nl.ing.api.contacting.conf.domain.model.connection;


public record ConnectionWithDetails(ConnectionVO connectionVO, ConnectionDetailsVO connectionDetailsVO,
                                    Boolean active) {
}
