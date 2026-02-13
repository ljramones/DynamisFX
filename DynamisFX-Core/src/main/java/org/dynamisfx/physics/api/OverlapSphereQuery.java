package org.dynamisfx.physics.api;

import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Sphere overlap query in world coordinates.
 */
public record OverlapSphereQuery(
        PhysicsVector3 center,
        double radiusMeters,
        int maxResults) {

    public OverlapSphereQuery {
        if (center == null) {
            throw new IllegalArgumentException("center must not be null");
        }
        if (!Double.isFinite(radiusMeters) || radiusMeters <= 0.0) {
            throw new IllegalArgumentException("radiusMeters must be finite and > 0");
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException("maxResults must be > 0");
        }
    }
}
