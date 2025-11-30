package nl.ing.api.contacting.conf.helper;

import com.ing.api.contacting.dto.java.resource.connection.ConnectionModel;
import com.ing.api.contacting.dto.java.resource.connection.ConnectionType;
import com.ing.api.contacting.dto.java.resource.connection.Layer;
import nl.ing.api.contacting.conf.domain.entity.ActiveConnectionEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionDetailsEntity;
import nl.ing.api.contacting.conf.domain.entity.ConnectionEntity;
import nl.ing.api.contacting.conf.domain.model.connection.*;
import nl.ing.api.contacting.conf.resource.dto.ConnectionModelV1;

import java.util.List;
import java.util.Optional;

public class ConnectionTestData {

    public static ConnectionWithDetails getConnectionWithDetails() {
        ConnectionVO connectionVO = new ConnectionVO(
                1L,
                2L,
                Layer.BACKEND,
                "domainName"
        );

        ConnectionDetailsVO detailsVO = new ConnectionDetailsVO(
                1L,
                2L,
                ConnectionType.PRIMARY,
                "dummyA",
                "dummyB",
                "dummyC"
        );

        Boolean isActive = true;

        return new ConnectionWithDetails(
                connectionVO,
                detailsVO,
                isActive
        );
    }

    public static ConnectionWithDetails getConnectionWithDetailsFrontEnd() {
        ConnectionVO connectionVO = new ConnectionVO(
                1L,
                2L,
                Layer.FRONTEND,
                "domainName"
        );

        ConnectionDetailsVO detailsVO = new ConnectionDetailsVO(
                1L,
                2L,
                ConnectionType.PRIMARY,
                "dummyA",
                "dummyB",
                "dummyC"
        );

        Boolean isActive = true;

        return new ConnectionWithDetails(
                connectionVO,
                detailsVO,
                isActive
        );
    }

    public static WebhookConnectionVO getWebhookConnectionVO() {
        return new WebhookConnectionVO(
                2L,
                ConnectionType.PRIMARY,
                "ing.com",
                3L,
                true
        );
    }

    public static ConnectionModelV1.TwilioDomain getTwilioDomainV1() {
        return new ConnectionModelV1.TwilioDomain(
                Optional.of(1L),
                "domainName",
                ConnectionType.PRIMARY,
                getUrlsV1()
        );
    }

    public static ConnectionModel.TwilioDomain getTwilioDomain() {
        return new ConnectionModel.TwilioDomain(
                Optional.of(1L),
                "domainName",
                ConnectionType.PRIMARY,
                getUrls()
        );
    }

    public static ConnectionModelV1.Connection getConnectionV1() {
        ConnectionModelV1.TwilioDomain dummyTwilioDomain = getTwilioDomainV1();

        return new ConnectionModelV1.Connection(
                1001L,
                List.of(dummyTwilioDomain),
                List.of(dummyTwilioDomain),
                Optional.empty()
        );
    }

    public static ConnectionModel.Connection getConnection() {
        ConnectionModel.TwilioDomain dummyTwilioDomain = new ConnectionModel.TwilioDomain(
                Optional.of(1L),
                "domainName",
                ConnectionType.PRIMARY,
                getUrls()
        );

        return new ConnectionModel.Connection(
                1001L,
                List.of(dummyTwilioDomain),
                List.of(dummyTwilioDomain),
                Optional.empty()
        );
    }

    public static ConnectionModelV1.URLs getUrlsV1() {
        ConnectionModelV1.UrlDetails primary = new ConnectionModelV1.UrlDetails
                (Optional.of(100L), "Leeuwarden", "https://primary.test.com" );

        ConnectionModelV1.UrlDetails fallback = new ConnectionModelV1.UrlDetails
                (Optional.of(101L), "Amsterdam", "https://fallback.test.com" );

        ConnectionModelV1.UrlDetails wfh = new ConnectionModelV1.UrlDetails
                (Optional.empty(), "Home Office", "https://wfh.test.com" );

        return new ConnectionModelV1.URLs(
                primary,
                Optional.of(fallback),
                Optional.of(wfh)
        );
    }

    public static ConnectionModel.URLs getUrls() {
        ConnectionModel.UrlDetails primary = new ConnectionModel.UrlDetails
                (Optional.of(100L), "Leeuwarden", "https://primary.test.com", Optional.of("ie1") );

        ConnectionModel.UrlDetails fallback = new ConnectionModel.UrlDetails
                (Optional.of(101L), "Amsterdam", "https://fallback.test.com", Optional.of("ie1") );

        ConnectionModel.UrlDetails wfh = new ConnectionModel.UrlDetails
                (Optional.empty(), "Home Office", "https://wfh.test.com", Optional.of("ie1"));

        return new ConnectionModel.URLs(
                primary,
                Optional.of(fallback),
                Optional.of(wfh)
        );
    }

    public static ConnectionVO getConnectionVO(Layer layer) {
        return new ConnectionVO(
                1L,
                2L,
                layer,
                "domainName"
        );
    }

    public static ConnectionDetailsEntity createDummyConnectionDetails(ConnectionEntity connection) {
        return ConnectionDetailsEntity.builder()
                .id(101L)
                .connectionType(ConnectionType.PRIMARY.getValue())
                .url("https://dummy.api.com")
                .connection(connection)
                .build();
    }

    public static ConnectionEntity createDummyConnectionEntity() {
        ConnectionEntity connection = ConnectionEntity.builder()
                .id(1L)
                .accountId(9999L)
                .layer(Layer.BACKEND.getValue())
                .domain("dummy.domain.com")
                .build();

        ConnectionDetailsEntity details = createDummyConnectionDetails(connection);
        connection.setDetails(List.of(details));

        return connection;
    }

    public static ActiveConnectionEntity createDummyActiveConnectionEntity() {
        ConnectionEntity connection = createDummyConnectionEntity();
        ConnectionDetailsEntity details = connection.getDetails().get(0);

        return ActiveConnectionEntity.builder()
                .connectionId(connection.getId())
                .connection(connection)
                .connectionDetails(details)
                .build();
    }


    public static ConnectionDetailsEntity createConnectionDetailsEntity() {
        ConnectionEntity connection = createDummyConnectionEntity();

        return ConnectionDetailsEntity.builder()
                .id(101L)
                .connection(connection)
                .connectionType(ConnectionType.WORK_FROM_HOME.getValue())
                .edgeLocation("AMS1")
                .url("https://api.test.com")
                .region("EU")
                .build();
    }


}
