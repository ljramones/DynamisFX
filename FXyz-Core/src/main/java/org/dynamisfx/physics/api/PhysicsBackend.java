package org.dynamisfx.physics.api;

import org.dynamisfx.physics.model.PhysicsWorldConfiguration;

/**
 * Backend provider for creating engine worlds.
 */
public interface PhysicsBackend extends AutoCloseable {

    String id();

    PhysicsCapabilities capabilities();

    PhysicsWorld createWorld(PhysicsWorldConfiguration configuration);

    @Override
    default void close() {
        // no-op
    }
}
