package org.fxyz3d.physics.hybrid;

import java.util.Map;
import org.fxyz3d.physics.api.PhysicsBodyHandle;
import org.fxyz3d.physics.model.PhysicsBodyState;

/**
 * Immutable snapshot of both worlds at a simulation instant.
 */
public record HybridSnapshot(
        double simulationTimeSeconds,
        Map<PhysicsBodyHandle, PhysicsBodyState> generalStates,
        Map<PhysicsBodyHandle, PhysicsBodyState> orbitalStates) {

    public HybridSnapshot {
        if (!Double.isFinite(simulationTimeSeconds) || simulationTimeSeconds < 0.0) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite and >= 0");
        }
        if (generalStates == null || orbitalStates == null) {
            throw new IllegalArgumentException("generalStates and orbitalStates must not be null");
        }
        generalStates = Map.copyOf(generalStates);
        orbitalStates = Map.copyOf(orbitalStates);
    }
}
