package org.fxyz3d.collision;

/**
 * Collision world event for a pair in a frame.
 */
public record CollisionEvent<T>(
        CollisionPair<T> pair,
        CollisionEventType type,
        boolean responseEnabled,
        ContactManifold3D manifold) {

    public CollisionEvent {
        if (pair == null || type == null) {
            throw new IllegalArgumentException("pair and type must not be null");
        }
    }
}
