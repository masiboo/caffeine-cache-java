package nl.ing.api.contacting.conf.util;

import nl.ing.api.contacting.conf.exception.Errors;

import java.util.Optional;

public class ValidationUtils {
    private static final String DEFAULT_FIELD_NAME = "Entity";

    private ValidationUtils() {}

    public static Long validatedLong(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw Errors.badRequest(fieldName +" ID is invalid " + value);
        }
        return value;
    }

    public static Long validatedLong(Long value) {
        return validatedLong(value, DEFAULT_FIELD_NAME);
    }

    public static Optional<Long> validatedOptionalLong(Optional<Long> optionalValue, String fieldName) {
        return optionalValue.map(value -> validatedLong(value, fieldName));
    }

    public static Optional<Long> validatedOptionalLong(Optional<Long> optionalValue) {
        return validatedOptionalLong(optionalValue, DEFAULT_FIELD_NAME);
    }

    /**
     * Validates an optional Long that can be empty (returns Optional.empty())
     * but when present must be valid (> 0 and not null)
     */
    public static Optional<Long> validatedOptionalLongAllowEmpty(Optional<Long> optionalValue, String fieldName) {
        return optionalValue.isEmpty()
                ? Optional.empty()
                : Optional.of(validatedLong(optionalValue.get(), fieldName));
    }

    public static Optional<Long> validatedOptionalLongAllowEmpty(Optional<Long> optionalValue) {
        return validatedOptionalLongAllowEmpty(optionalValue, DEFAULT_FIELD_NAME);
    }

}
