package nl.ing.api.contacting.conf.logging;

import ch.qos.logback.access.common.spi.IAccessEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TraceIdConverterTest {

    private final TraceIdConverter converter = new TraceIdConverter();

    @Test
    void testConvertReturnsXB3TraceIdIfPresent() {
        IAccessEvent event = mock(IAccessEvent.class);
        when(event.getRequestHeader("x-b3-traceid")).thenReturn("trace123");
        when(event.getRequestHeader("uber-trace-id")).thenReturn("uber456:abc");

        String result = converter.convert(event);
        assertEquals("trace123", result);
    }

    @Test
    void testConvertReturnsUberTraceIdIfXB3Missing() {
        IAccessEvent event = mock(IAccessEvent.class);
        when(event.getRequestHeader("x-b3-traceid")).thenReturn(null);
        when(event.getRequestHeader("uber-trace-id")).thenReturn("uber456:abc");

        String result = converter.convert(event);
        assertEquals("uber456:abc", result);
    }

    @Test
    void testConvertReturnsDashIfNoTraceId() {
        IAccessEvent event = mock(IAccessEvent.class);
        when(event.getRequestHeader("x-b3-traceid")).thenReturn("-");
        when(event.getRequestHeader("uber-trace-id")).thenReturn("-");

        String result = converter.convert(event);
        assertEquals("-", result);
    }

    @Test
    void testConvertReturnsDashIfHeadersNull() {
        IAccessEvent event = mock(IAccessEvent.class);
        when(event.getRequestHeader("x-b3-traceid")).thenReturn(null);
        when(event.getRequestHeader("uber-trace-id")).thenReturn(null);

        String result = converter.convert(event);
        assertEquals("-", result);
    }
}