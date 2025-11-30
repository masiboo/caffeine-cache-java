package nl.ing.api.contacting.conf.configuration;

import nl.ing.api.contacting.conf.exception.GlobalExceptionHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class JerseyConfiguration {

    @Bean
    protected Consumer<ResourceConfig> customerConfig()  {
        return resourceConfig -> {
            resourceConfig.register(GlobalExceptionHandler.class);
        };
    }
}
