package nl.ing.api.contacting.conf.logging;

import ch.qos.logback.access.common.pattern.AccessConverter;
import ch.qos.logback.access.common.spi.IAccessEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class TraceIdConverter extends AccessConverter {

    @Override
    public String convert(IAccessEvent event) {
        var requestHeader = event.getRequestHeader("x-b3-traceid");
        if (StringUtils.isNotBlank(requestHeader) && !"-".equals(requestHeader)) {
            return requestHeader;
        } else {
            return Optional.ofNullable(event.getRequestHeader("uber-trace-id"))
                    .filter(s -> !"-".equals(s))
                    .map(uberTrace -> uberTrace.replaceFirst("%.*", ""))
                    .orElse("-");
        }

    }
}
