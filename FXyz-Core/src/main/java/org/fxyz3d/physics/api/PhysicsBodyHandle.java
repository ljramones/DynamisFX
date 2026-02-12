package org.fxyz3d.physics.api;

/**
 * Opaque body identifier allocated by a physics world.
 */
public record PhysicsBodyHandle(long value) {

    public PhysicsBodyHandle {
        if (value < 0L) {
            throw new IllegalArgumentException("handle value must be >= 0");
        }
    }
}
