package org.fxyz3d.physics.model;

/**
 * Sphere shape (radius in meters).
 */
public record SphereShape(double radius) implements PhysicsShape {

    public SphereShape {
        if (!(radius > 0.0)) {
            throw new IllegalArgumentException("radius must be > 0");
        }
    }
}
