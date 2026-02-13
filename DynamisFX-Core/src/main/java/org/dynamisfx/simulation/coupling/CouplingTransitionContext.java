package org.dynamisfx.simulation.coupling;

import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Objects;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Immutable context passed to transition policy evaluation.
 */
public record CouplingTransitionContext(
        String objectId,
        ObjectSimulationMode currentMode,
        double simulationTimeSeconds,
        double lastTransitionTimeSeconds,
        OptionalDouble predictedInterceptSeconds,
        Collection<PhysicsZone> zones) {

    public CouplingTransitionContext {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        Objects.requireNonNull(currentMode, "currentMode must not be null");
        Objects.requireNonNull(predictedInterceptSeconds, "predictedInterceptSeconds must not be null");
        Objects.requireNonNull(zones, "zones must not be null");
        if (!Double.isFinite(simulationTimeSeconds)) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite");
        }
        if (!Double.isFinite(lastTransitionTimeSeconds)) {
            throw new IllegalArgumentException("lastTransitionTimeSeconds must be finite");
        }
    }
}
