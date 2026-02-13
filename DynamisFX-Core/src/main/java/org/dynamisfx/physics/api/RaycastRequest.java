package org.dynamisfx.physics.api;

import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * World-space raycast request.
 */
public record RaycastRequest(
        PhysicsVector3 origin,
        PhysicsVector3 direction,
        double maxDistanceMeters) {

    public RaycastRequest {
        if (origin == null || direction == null) {
            throw new IllegalArgumentException("origin and direction must not be null");
        }
        if (!Double.isFinite(maxDistanceMeters) || maxDistanceMeters <= 0.0) {
            throw new IllegalArgumentException("maxDistanceMeters must be finite and > 0");
        }
        double n2 = (direction.x() * direction.x()) + (direction.y() * direction.y()) + (direction.z() * direction.z());
        if (!(n2 > 0.0)) {
            throw new IllegalArgumentException("direction norm must be > 0");
        }
    }
}
