package nl.ing.api.contacting.conf.logging;

import ch.qos.logback.access.common.spi.AccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnrichedAccessEventTest {

    private AccessEvent accessEvent;
    private EnrichedAccessEvent enrichedEvent;

    @BeforeEach
    void setUp() {
        accessEvent = mock(AccessEvent.class);
        when(accessEvent.getThreadName()).thenReturn("main-thread");
        when(accessEvent.getRequestURI()).thenReturn("/api/test");
        when(accessEvent.getElapsedTime()).thenReturn(123L);
        when(accessEvent.getMethod()).thenReturn("GET");
        when(accessEvent.getRemoteHost()).thenReturn("127.0.0.1");
        when(accessEvent.getStatusCode()).thenReturn(200);
        when(accessEvent.getTimeStamp()).thenReturn(1650000000000L);
        when(accessEvent.getRequestHeaderMap()).thenReturn(Map.of("uber-trace-id", "abc123:xyz"));
        when(accessEvent.getRequestHeader("x-ing-twilio-cjid")).thenReturn("cjid-1");
        when(accessEvent.getRequestHeader("x-ing-request-host")).thenReturn("host-1");
        when(accessEvent.getRequestHeader("Accept")).thenReturn("application/json");
        when(accessEvent.getRequestHeader("X-Forwarded-For")).thenReturn("10.0.0.1");
        when(accessEvent.getRequestHeader("referer")).thenReturn("http://referer");
        when(accessEvent.getAttribute("__twilio_sub_account__")).thenReturn("subacc-1");

        enrichedEvent = new EnrichedAccessEvent(accessEvent, "componentA", "envB");
    }

    @Test
    void testBasicProperties() {
        assertEquals("main-thread", enrichedEvent.getThreadName());
        assertEquals("/api/test", enrichedEvent.getMessage());
        assertEquals("access_logger", enrichedEvent.getLoggerName());
        assertEquals(1650000000000L, enrichedEvent.getTimeStamp());
        assertEquals("access", enrichedEvent.getMarker().getName());
        assertEquals("componentA", enrichedEvent.getMDCPropertyMap().get("component"));
        assertEquals("envB", enrichedEvent.getMDCPropertyMap().get("environment"));
    }

    @Test
    void testMDCPropertyMapContainsExpectedKeys() {
        Map<String, String> mdc = enrichedEvent.getMDCPropertyMap();
        assertTrue(mdc.containsKey("timestamp_ms"));
        assertTrue(mdc.containsKey("duration"));
        assertTrue(mdc.containsKey("request"));
        assertTrue(mdc.containsKey("verb"));
        assertTrue(mdc.containsKey("clientip"));
        assertTrue(mdc.containsKey("trace_id"));
        assertTrue(mdc.containsKey("response"));
        assertEquals("cjid-1", mdc.get("cjid"));
        assertEquals("host-1", mdc.get("x-ing-request-host"));
        assertEquals("application/json", mdc.get("accept"));
        assertEquals("10.0.0.1", mdc.get("X-Forwarded-For"));
        assertEquals("http://referer", mdc.get("referer"));
        assertEquals("subacc-1", mdc.get("sub_account"));
    }

    @Test
    void testGetLevelIsInfo() {
        assertEquals(ch.qos.logback.classic.Level.INFO, enrichedEvent.getLevel());
    }

    @Test
    void testGetThrowableProxyIsNull() {
        assertNull(enrichedEvent.getThrowableProxy());
    }

    @Test
    void testGetCallerDataIsEmpty() {
        assertEquals(0, enrichedEvent.getCallerData().length);
        assertFalse(enrichedEvent.hasCallerData());
    }
}