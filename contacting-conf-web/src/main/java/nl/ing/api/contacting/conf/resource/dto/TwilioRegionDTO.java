package nl.ing.api.contacting.conf.resource.dto;

import java.io.Serializable;
import java.util.List;

public final class TwilioRegionDTO implements Serializable {

    public record Region(String name) implements Serializable { }
    public record Regions(List<Region> regions) implements Serializable { }

    private static final List<Region> ALL = List.of(
            new Region("ie1"),
            new Region("au1"),
            new Region("us1")
    );

    public static Regions allRegions() {
        return new Regions(ALL);
    }

    public static List<String> regionNames() {
        return ALL.stream()
                .map(Region::name)
                .toList();
    }
}
