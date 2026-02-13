package org.dynamisfx.physics.model;

/**
 * Immutable body state snapshot.
 */
public record PhysicsBodyState(
        PhysicsVector3 position,
        PhysicsQuaternion orientation,
        PhysicsVector3 linearVelocity,
        PhysicsVector3 angularVelocity,
        ReferenceFrame referenceFrame,
        double timestampSeconds) {

    public static final PhysicsBodyState IDENTITY = new PhysicsBodyState(
            PhysicsVector3.ZERO,
            PhysicsQuaternion.IDENTITY,
            PhysicsVector3.ZERO,
            PhysicsVector3.ZERO,
            ReferenceFrame.UNSPECIFIED,
            0.0);

    public PhysicsBodyState {
        if (position == null || orientation == null
                || linearVelocity == null || angularVelocity == null
                || referenceFrame == null) {
            throw new IllegalArgumentException("state values must not be null");
        }
        if (!Double.isFinite(timestampSeconds)) {
            throw new IllegalArgumentException("timestampSeconds must be finite");
        }
    }
}
