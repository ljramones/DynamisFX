package org.dynamisfx.physics.hybrid;

import java.util.Map;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;

/**
 * Immutable snapshot of both worlds at a simulation instant.
 */
public record HybridSnapshot(
        double simulationTimeSeconds,
        double interpolationAlpha,
        double extrapolationSeconds,
        Map<PhysicsBodyHandle, PhysicsBodyState> generalStates,
        Map<PhysicsBodyHandle, PhysicsBodyState> orbitalStates) {

    public HybridSnapshot {
        if (!Double.isFinite(simulationTimeSeconds) || simulationTimeSeconds < 0.0) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite and >= 0");
        }
        if (!Double.isFinite(interpolationAlpha) || interpolationAlpha < 0.0 || interpolationAlpha > 1.0) {
            throw new IllegalArgumentException("interpolationAlpha must be finite and in [0,1]");
        }
        if (!Double.isFinite(extrapolationSeconds) || extrapolationSeconds < 0.0) {
            throw new IllegalArgumentException("extrapolationSeconds must be finite and >= 0");
        }
        if (generalStates == null || orbitalStates == null) {
            throw new IllegalArgumentException("generalStates and orbitalStates must not be null");
        }
        generalStates = Map.copyOf(generalStates);
        orbitalStates = Map.copyOf(orbitalStates);
    }
}
