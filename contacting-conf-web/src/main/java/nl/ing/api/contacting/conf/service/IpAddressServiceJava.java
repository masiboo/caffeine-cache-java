package nl.ing.api.contacting.conf.service;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.entity.PlatformAccountSettingsEntity;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.repository.PlatformAccountSettingsCacheRepository;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Migrated from Scala: Used to check for WFH indicator.
 * Uses platform account settings to check IP_SUBNET_RANGE stored for each account.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IpAddressServiceJava {

    private static final String IP_SUBNET_RANGE_KEY = "IP_SUBNET_RANGE";
    private static final String COMMA = ",";

    private final PlatformAccountSettingsCacheRepository platformAccountSettingsCacheRepository;


    /**
     * Fetches WFH indicator for the given IP and context.
     */
    public Boolean fetchWFHIndicator(String xForwardedForValue, ContactingContext context) {
        if (xForwardedForValue == null || xForwardedForValue.isBlank()) {
            log.debug("xForwardedForValue is null or blank for account: {}", context.accountId());
            return false;
        }
        String clientIpAddress = xForwardedForValue.split(COMMA)[0].trim();
        boolean wfh = inAccountSubnet(clientIpAddress, context);
        log.debug("Fetched WFH indicator for {} for account: {} - {}", context.auditContext().modifiedBy(), context.accountId(), wfh);
        return wfh;
    }

    private boolean inAccountSubnet(String ipAddress, ContactingContext context) {
        List<PlatformAccountSettingsEntity> settings = platformAccountSettingsCacheRepository.findByAccountId(context);
        if (settings == null || settings.isEmpty()) {
            throw Errors.notFound(String.valueOf(context.accountId()));
        }
        return settings.stream()
                .filter(setting -> IP_SUBNET_RANGE_KEY.equals(setting.getKey()))
                .map(PlatformAccountSettingsEntity::getValue)
                .findFirst()
                .map(ranges -> belongsTo(ipAddress, ranges))
                .orElseGet(() -> false);
    }

    private Boolean belongsTo(String ipAddress, String ipSubnetRangeValue) {
        return Arrays.stream(ipSubnetRangeValue.split(COMMA))
                .map(String::trim)
                .anyMatch(range -> contains(range, ipAddress));
    }

    private boolean contains(String ipSubnet, String ipAddress) {
        SubnetUtils accountSubnet = new SubnetUtils(ipSubnet);
        accountSubnet.setInclusiveHostCount(true);
        return accountSubnet.getInfo().isInRange(ipAddress);
    }
}
