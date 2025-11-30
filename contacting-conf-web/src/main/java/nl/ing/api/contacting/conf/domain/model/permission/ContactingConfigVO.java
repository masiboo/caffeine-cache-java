package nl.ing.api.contacting.conf.domain.model.permission;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record ContactingConfigVO(String key, String values) {

    public Set<String> valuesAsSet() {
        return Arrays.stream(values.split(","))
                .collect(Collectors.toSet());
    }
}