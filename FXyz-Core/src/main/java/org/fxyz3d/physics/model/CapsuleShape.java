package org.fxyz3d.physics.model;

/**
 * Capsule shape (radius and straight section length in meters).
 */
public record CapsuleShape(double radius, double length) implements PhysicsShape {

    public CapsuleShape {
        if (!(radius > 0.0) || !(length >= 0.0)) {
            throw new IllegalArgumentException("capsule radius must be > 0 and length must be >= 0");
        }
    }
}
