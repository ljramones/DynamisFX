package org.dynamisfx.physics.jolt;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;

/**
 * Jolt backend shell. Native functionality is provided through a C shim loaded at runtime.
 */
public final class JoltBackend implements PhysicsBackend {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            true,
            false,
            true,
            true,
            true);

    private final JoltNativeBridge bridge;

    public JoltBackend() {
        this(new JoltNativeBridge());
    }

    JoltBackend(JoltNativeBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public String id() {
        return "jolt";
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public PhysicsWorld createWorld(PhysicsWorldConfiguration configuration) {
        return new JoltWorld(configuration, bridge);
    }
}
