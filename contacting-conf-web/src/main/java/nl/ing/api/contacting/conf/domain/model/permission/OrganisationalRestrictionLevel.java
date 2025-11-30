package nl.ing.api.contacting.conf.domain.model.permission;

import lombok.Getter;
import nl.ing.api.contacting.conf.exception.Errors;

import java.util.Arrays;

@Getter
public enum OrganisationalRestrictionLevel {
    NONE("NONE", 0),
    SELF("SELF", 1),
    TEAM("TEAM", 2),
    CIRCLE("CIRCLE", 3),
    SUPER_CIRCLE("SUPER_CIRCLE", 4),
    ACCOUNT("ACCOUNT", 5);

    private final String value;
    private final int level;

    OrganisationalRestrictionLevel(String value, int level) {
        this.value = value;
        this.level = level;
    }

    public static OrganisationalRestrictionLevel fromValue(String value) {
        return Arrays.stream(values())
                .filter(v -> v.value.equals(value))
                .findFirst()
                .orElseThrow(() -> Errors.unexpected("Unknown value: " + value));
    }

    public static OrganisationalRestrictionLevel fromLevel(int level) {
        return Arrays.stream(values())
                .filter(v -> v.level == level)
                .findFirst()
                .orElseThrow(() -> Errors.unexpected("Unknown level: " + level));
    }

}