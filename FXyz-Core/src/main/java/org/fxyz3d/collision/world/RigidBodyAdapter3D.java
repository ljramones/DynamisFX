package org.fxyz3d.collision;

import org.fxyz3d.geometry.Vector3D;

/**
 * Adapter for reading and writing rigid-body state from user objects.
 */
public interface RigidBodyAdapter3D<T> {

    Vector3D getPosition(T body);

    void setPosition(T body, Vector3D position);

    Vector3D getVelocity(T body);

    void setVelocity(T body, Vector3D velocity);

    double getInverseMass(T body);

    double getRestitution(T body);

    double getFriction(T body);
}
