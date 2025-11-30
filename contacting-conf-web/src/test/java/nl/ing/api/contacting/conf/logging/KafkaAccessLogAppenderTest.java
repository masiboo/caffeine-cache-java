package nl.ing.api.contacting.conf.logging;

import ch.qos.logback.access.common.spi.AccessEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KafkaAccessLogAppenderTest {

    static class TestKafkaAccessLogAppender extends KafkaAccessLogAppender {
        Object appendedEvent;

        @Override
        protected void append(Object accessEvent) {
            appendedEvent = accessEvent;
        }
    }

    @Test
    void testAppendWrapsAccessEvent() {
        AccessEvent accessEvent = org.mockito.Mockito.mock(AccessEvent.class);

        TestKafkaAccessLogAppender appender = new TestKafkaAccessLogAppender();
        appender.setComponent("compX");
        appender.setEnvironment("envY");

        appender.append(accessEvent);

        assertNotNull(appender.appendedEvent);
        assertTrue(appender.appendedEvent instanceof AccessEvent);
    }
}