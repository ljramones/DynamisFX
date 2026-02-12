package org.fxyz3d.physics.api;

/**
 * Factory for creating backend instances.
 */
public interface PhysicsBackendFactory {

    String backendId();

    PhysicsBackend createBackend();
}
