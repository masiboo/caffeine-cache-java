package nl.ing.api.contacting.conf.logging;

import ch.qos.logback.access.common.spi.AccessEvent;
import ch.qos.logback.core.AsyncAppenderBase;
import com.ing.log.common.ConfigAttribute;
import lombok.Setter;

@Setter
public class KafkaAccessLogAppender extends AsyncAppenderBase {
    @ConfigAttribute
    private String component;

    @ConfigAttribute
    private String environment;

    @Override
    protected void append(Object accessEvent) {
        // not able to use generics here because AccessEvent is received, but inner appenders are using LoggingEvent
        super.append(new EnrichedAccessEvent((AccessEvent) accessEvent, component, environment));
    }

}
