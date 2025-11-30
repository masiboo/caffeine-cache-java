package nl.ing.api.contacting.conf.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.java.contacting.caching.core.ContactingCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@Slf4j
public class CacheConfiguration {

    private final ContactingCache contactingCache;

    public CacheConfiguration() {
        contactingCache = new ContactingCache() {
            @Override
            public Config config() {
                return ConfigFactory.parseFile(new File("cache.conf"))
                        .withFallback(ConfigFactory.load());
            }
        };
    }

    @Bean
    public ContactingCache contactingCache() {
        return contactingCache;
    }
}

