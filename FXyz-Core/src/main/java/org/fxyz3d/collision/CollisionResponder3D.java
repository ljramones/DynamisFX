package org.fxyz3d.collision;

/**
 * Optional collision-world response hook executed for response-enabled contacts.
 */
@FunctionalInterface
public interface CollisionResponder3D<T> {

    void resolve(CollisionEvent<T> event);
}
