package org.dynamisfx.physics.jolt;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsBackendFactory;

/**
 * Factory entry point for the optional Jolt backend.
 */
public final class JoltBackendFactory implements PhysicsBackendFactory {

    @Override
    public String backendId() {
        return "jolt";
    }

    @Override
    public PhysicsBackend createBackend() {
        return new JoltBackend();
    }
}
