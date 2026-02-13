package org.dynamisfx.physics.api;

import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Result of a successful raycast query.
 */
public record RaycastHit(
        PhysicsBodyHandle bodyHandle,
        PhysicsVector3 point,
        PhysicsVector3 normal,
        double distanceMeters) {

    public RaycastHit {
        if (bodyHandle == null || point == null || normal == null) {
            throw new IllegalArgumentException("bodyHandle, point and normal must not be null");
        }
        if (!Double.isFinite(distanceMeters) || distanceMeters < 0.0) {
            throw new IllegalArgumentException("distanceMeters must be finite and >= 0");
        }
    }
}
