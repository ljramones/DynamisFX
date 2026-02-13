package org.dynamisfx.simulation.orbital;

import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;

/**
 * Global orbital state snapshot produced by an orbital dynamics engine.
 */
public record OrbitalState(
        PhysicsVector3 position,
        PhysicsVector3 linearVelocity,
        PhysicsQuaternion orientation,
        ReferenceFrame referenceFrame,
        double timestampSeconds) {

    public OrbitalState {
        if (position == null || linearVelocity == null || orientation == null || referenceFrame == null) {
            throw new IllegalArgumentException("state values must not be null");
        }
        if (!Double.isFinite(timestampSeconds)) {
            throw new IllegalArgumentException("timestampSeconds must be finite");
        }
    }
}
