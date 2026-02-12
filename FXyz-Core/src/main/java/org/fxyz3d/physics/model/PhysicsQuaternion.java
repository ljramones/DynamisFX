package org.fxyz3d.physics.model;

/**
 * Immutable quaternion for orientation state.
 */
public record PhysicsQuaternion(double x, double y, double z, double w) {

    public static final PhysicsQuaternion IDENTITY = new PhysicsQuaternion(0.0, 0.0, 0.0, 1.0);

    public PhysicsQuaternion {
        if (!Double.isFinite(x) || !Double.isFinite(y)
                || !Double.isFinite(z) || !Double.isFinite(w)) {
            throw new IllegalArgumentException("quaternion values must be finite");
        }
    }
}
