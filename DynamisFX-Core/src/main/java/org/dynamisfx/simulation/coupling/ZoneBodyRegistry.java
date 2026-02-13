package org.dynamisfx.simulation.coupling;

import java.util.Objects;
import java.util.Optional;
import org.dynamisfx.physics.api.PhysicsBodyHandle;

/**
 * Tracks active rigid-body bindings for simulation objects inside physics zones.
 */
public final class ZoneBodyRegistry {

    private final java.util.Map<String, ZoneBodyBinding> bindingsByObjectId = new java.util.LinkedHashMap<>();

    public synchronized void bind(String objectId, ZoneId zoneId, PhysicsBodyHandle bodyHandle) {
        validateObjectId(objectId);
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        Objects.requireNonNull(bodyHandle, "bodyHandle must not be null");
        bindingsByObjectId.put(objectId, new ZoneBodyBinding(objectId, zoneId, bodyHandle));
    }

    public synchronized Optional<ZoneBodyBinding> bindingForObject(String objectId) {
        validateObjectId(objectId);
        return Optional.ofNullable(bindingsByObjectId.get(objectId));
    }

    public synchronized Optional<ZoneBodyBinding> unbind(String objectId) {
        validateObjectId(objectId);
        return Optional.ofNullable(bindingsByObjectId.remove(objectId));
    }

    private static void validateObjectId(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
    }

    public record ZoneBodyBinding(String objectId, ZoneId zoneId, PhysicsBodyHandle bodyHandle) {
        public ZoneBodyBinding {
            if (objectId == null || objectId.isBlank()) {
                throw new IllegalArgumentException("objectId must not be blank");
            }
            Objects.requireNonNull(zoneId, "zoneId must not be null");
            Objects.requireNonNull(bodyHandle, "bodyHandle must not be null");
        }
    }
}
