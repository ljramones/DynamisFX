package org.fxyz3d.collision;

import org.fxyz3d.geometry.Vector3D;

/**
 * Support mapping for convex 3D shapes.
 */
@FunctionalInterface
public interface ConvexSupport3D {

    /**
     * Returns the farthest point in the given direction.
     */
    Vector3D support(Vector3D direction);
}
