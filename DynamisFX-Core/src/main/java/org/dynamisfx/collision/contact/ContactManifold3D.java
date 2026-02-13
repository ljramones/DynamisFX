package org.dynamisfx.collision;

import java.util.List;

/**
 * Collision manifold with contact points.
 */
public record ContactManifold3D(
        CollisionManifold3D manifold,
        List<ContactPoint3D> contacts) {

    public ContactManifold3D {
        if (manifold == null) {
            throw new IllegalArgumentException("manifold must not be null");
        }
        if (contacts == null || contacts.isEmpty()) {
            throw new IllegalArgumentException("contacts must not be null or empty");
        }
        contacts = List.copyOf(contacts);
    }
}
