package nl.ing.api.contacting.conf.configuration;

import com.ing.apisdk.toolkit.logging.audit.api.AuditContext;
import com.ing.apisdk.toolkit.logging.audit.api.AuditEvent;
import com.ing.apisdk.toolkit.logging.audit.api.AuditLogger;
import com.ing.apisdk.toolkit.logging.audit.slf4j.Slf4jAuditLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@Slf4j
public class AuditLoggerService {
    private static final String AUDIT_APPLICATION_ID = "APIRegistryAPI";
    private AuditLogger auditLogger;

    public AuditLoggerService() {
        try {
            this.auditLogger = new Slf4jAuditLogger(new AuditContext(
                    AUDIT_APPLICATION_ID, InetAddress.getLocalHost().getHostName()));
        } catch (UnknownHostException e) {
            log.error("Could not get hostname for audit log", e.getMessage());
        }
    }

    public void logAuditEvent(AuditEvent  auditEvent) {
        auditLogger.log(auditEvent);
    }
}



