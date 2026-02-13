package org.dynamisfx.physics.api;

/**
 * Opaque constraint identifier allocated by a physics world.
 */
public record PhysicsConstraintHandle(long value) {

    public PhysicsConstraintHandle {
        if (value < 0L) {
            throw new IllegalArgumentException("constraint handle value must be >= 0");
        }
    }
}
