package org.dynamisfx.simulation.coupling;

/**
 * Stable identifier for a local physics zone.
 */
public record ZoneId(String value) {

    public ZoneId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
