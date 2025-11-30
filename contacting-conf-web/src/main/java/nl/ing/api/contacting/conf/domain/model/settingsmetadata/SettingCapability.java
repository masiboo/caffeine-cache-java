package nl.ing.api.contacting.conf.domain.model.settingsmetadata;

import com.fasterxml.jackson.annotation.JsonValue;
import nl.ing.api.contacting.conf.exception.Errors;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Java enum representing the capabilities of a setting.
 */
public enum SettingCapability {
    CHAT(0, "chat"),
    VIDEO(1, "video"),
    RECORDING(2, "recording"),
    DASHBOARD(3, "dashboard"),
    GENERIC(4, "generic"),
    DIALER(5, "dialer");

    private final int id;
    private final String value;

    SettingCapability(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public static SettingCapability fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(SettingCapability.values())
                .filter(settingCapability -> settingCapability.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> Errors.notFound("capability query param found but no value"));
    }

    public int id() {
        return id;
    }

    @JsonValue
    public String value() {
        return value;
    }

}
