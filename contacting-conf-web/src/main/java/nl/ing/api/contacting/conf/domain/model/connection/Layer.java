package nl.ing.api.contacting.conf.domain.model.connection;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import nl.ing.api.contacting.conf.exception.Errors;

import java.util.Arrays;

@Getter
public enum Layer {

    FRONTEND("frontend"),
    BACKEND("backend");

    private final String value;

    Layer(String value) {
        this.value = value;
    }

    public static Layer fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(Layer.values())
                .filter(layer -> layer.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> Errors.notFound("Unknown layer: " + value));
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
