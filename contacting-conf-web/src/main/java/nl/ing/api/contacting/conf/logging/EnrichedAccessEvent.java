package nl.ing.api.contacting.conf.logging;

import ch.qos.logback.access.common.spi.AccessEvent;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.KeyValuePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Merak25: 5th Manual change; new abstract methods have to be over-ridden
 */
@Slf4j
public class EnrichedAccessEvent implements ILoggingEvent {

    private final AccessEvent accessEvent;
    private final String component;
    private final String environment;

    public EnrichedAccessEvent(final AccessEvent accessEvent, String component, String environment) {
        this.accessEvent = accessEvent;
        this.component = component;
        this.environment = environment;
    }


    @Override
    public String getThreadName() {
        return accessEvent.getThreadName();
    }

    @Override
    public Level getLevel() {
        return Level.INFO;
    }

    @Override
    public String getMessage() {
        return accessEvent.getRequestURI();
    }

    @Override
    public Object[] getArgumentArray() {
        return new Object[0];
    }

    @Override
    public String getFormattedMessage() {
        return this.getMessage();
    }

    @Override
    public String getLoggerName() {
        return "access_logger";
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        return new LoggerContextVO("access", new HashMap<>(), System.currentTimeMillis());
    }

    @Override
    public IThrowableProxy getThrowableProxy() {
        // access logs dont have stacktraces/throwables
        return null;
    }

    @Override
    public StackTraceElement[] getCallerData() {
        return new StackTraceElement[0];
    }

    @Override
    public boolean hasCallerData() {
        return false;
    }

    @Override
    public Marker getMarker() {
        return MarkerFactory.getMarker("access");
    }

    @Override
    public List<Marker> getMarkerList() {
        return null;
    }

    @Override
    public Map<String, String> getMDCPropertyMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("timestamp_ms", String.valueOf(System.currentTimeMillis()));
        map.put("duration", String.valueOf(this.accessEvent.getElapsedTime()));
        map.put("request", this.accessEvent.getRequestURI());
        map.put("verb", this.accessEvent.getMethod());
        map.put("clientip", this.accessEvent.getRemoteHost());

        final String traceId = this.accessEvent.getRequestHeaderMap().entrySet().stream()
                .filter(e -> e.getKey().equals("uber-trace-id"))
                .map(Map.Entry::getValue)
                .findFirst()
                .map(s -> s.split("(%3A)|:")[0])
                .orElse("-");


        map.put("trace_id", traceId);

        map.put("response", String.valueOf(this.accessEvent.getStatusCode()));
        Optional.ofNullable(this.accessEvent.getRequestHeader("x-ing-twilio-cjid")).ifPresent(referer -> map.put("cjid", referer));
        Optional.ofNullable(this.accessEvent.getRequestHeader("x-ing-request-host")).ifPresent(referer -> map.put("x-ing-request-host", referer));
        Optional.ofNullable(this.accessEvent.getRequestHeader("Accept")).ifPresent(referer -> map.put("accept", referer));
        Optional.ofNullable(this.accessEvent.getRequestHeader("X-Forwarded-For")).ifPresent(referer -> map.put("X-Forwarded-For", referer));
        Optional.ofNullable(this.accessEvent.getRequestHeader("referer")).ifPresent(referer -> map.put("referer", referer));
        Optional.ofNullable(this.accessEvent.getAttribute("__twilio_sub_account__")).ifPresent(referer -> map.put("sub_account", referer));
        map.put("component", this.component);
        map.put("environment", this.environment);
        return map;
    }

    @Override
    public Map<String, String> getMdc() {
        return getMDCPropertyMap();
    }

    @Override
    public long getTimeStamp() {
        return accessEvent.getTimeStamp();
    }

    @Override
    public int getNanoseconds() {
        return 0;
    }

    @Override
    public long getSequenceNumber() {
        return 0;
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {

        return null;
    }

    @Override
    public void prepareForDeferredProcessing() {

    }
}
