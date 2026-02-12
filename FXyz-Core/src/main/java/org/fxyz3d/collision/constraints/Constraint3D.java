package org.fxyz3d.collision;

/**
 * Positional/velocity constraint solved during a physics step.
 */
public interface Constraint3D<T> {

    void solve(RigidBodyAdapter3D<T> adapter, double dtSeconds);
}
