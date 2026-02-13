package org.dynamisfx.physics.model;

/**
 * Immutable 3D vector for engine-agnostic physics state.
 */
public record PhysicsVector3(double x, double y, double z) {

    public static final PhysicsVector3 ZERO = new PhysicsVector3(0.0, 0.0, 0.0);

    public PhysicsVector3 {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("vector values must be finite");
        }
    }
}
