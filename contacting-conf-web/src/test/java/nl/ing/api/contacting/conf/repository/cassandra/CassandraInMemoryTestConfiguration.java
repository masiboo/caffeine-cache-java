package nl.ing.api.contacting.conf.repository.cassandra;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import jakarta.annotation.PreDestroy;
import nl.ing.api.contacting.test.configuration.ContactingCassandraUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.cassandra.config.CassandraEntityClassScanner;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver;
import org.springframework.data.cassandra.core.mapping.UserTypeResolver;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "nl.ing.api.contacting.conf.repository")
public class CassandraInMemoryTestConfiguration extends ContactingCassandraUnit {
    private static final String CASSANDRA_KEYSPACE = "contacting";

    public CassandraInMemoryTestConfiguration() {
        super("cassandra/init.cql", CASSANDRA_KEYSPACE);
        this.before();
    }

    @PreDestroy
    public void cleanUp() {
        this.after();
    }

    @Bean
    @Lazy
    public CassandraOperations cassandraTemplate(CqlSession session, CassandraConverter converter) {
        return new CassandraTemplate(session, converter);
    }

    @Bean("testOperations")
    @Lazy
    public CassandraOperations testOperations(CqlSession session, CassandraConverter converter) {
        return new CassandraTemplate(session, converter);
    }

    @Bean("merakTracer")
    public Tracer tracer() {
        return TracerProvider.noop().get("noop");
    }

    @Bean
    @Lazy
    public CassandraConverter converter(CassandraMappingContext mappingContext, CqlSession session) {
        UserTypeResolver resolver = new SimpleUserTypeResolver(session, CqlIdentifier.fromInternal(CASSANDRA_KEYSPACE));
        MappingCassandraConverter converter = new MappingCassandraConverter(mappingContext);
        converter.setUserTypeResolver(resolver);
        return converter;
    }

    @Bean
    @Lazy
    public CqlSession cassandraSession() {
        return getSession();
    }

    @Bean
    public CassandraMappingContext mappingContext() throws ClassNotFoundException {
        CassandraMappingContext mappingContext = new CassandraMappingContext();
        mappingContext.setInitialEntitySet(CassandraEntityClassScanner.scan("nl.ing.api.contacting.conf.repository"));
        return mappingContext;
    }

    //Needed here because the @Configuration is excluded from component scan so other beans need to be defined here for tests.
    @Bean
    public Gson getGson() {
        return new Gson();
    }

    @Bean
    public Config applicationConf() {
        return ConfigFactory.load("application.conf");
    }

}
