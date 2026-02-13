package org.dynamisfx.simulation.coupling;

import java.util.Objects;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Captures seed values used when handing off state between global orbital and local rigid simulation.
 */
public record StateHandoffSnapshot(
        StateHandoffDirection direction,
        double simulationTimeSeconds,
        String objectId,
        ZoneId zoneId,
        PhysicsVector3 zoneAnchorPosition,
        PhysicsVector3 globalPosition,
        PhysicsVector3 globalVelocity,
        PhysicsVector3 localPosition,
        PhysicsVector3 localVelocity) {

    public StateHandoffSnapshot {
        Objects.requireNonNull(direction, "direction must not be null");
        if (!Double.isFinite(simulationTimeSeconds)) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite");
        }
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        Objects.requireNonNull(zoneAnchorPosition, "zoneAnchorPosition must not be null");
        Objects.requireNonNull(globalPosition, "globalPosition must not be null");
        Objects.requireNonNull(globalVelocity, "globalVelocity must not be null");
        Objects.requireNonNull(localPosition, "localPosition must not be null");
        Objects.requireNonNull(localVelocity, "localVelocity must not be null");
    }
}
