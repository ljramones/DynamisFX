package org.fxyz3d.physics.orekit;

import org.fxyz3d.physics.api.PhysicsBackend;
import org.fxyz3d.physics.api.PhysicsCapabilities;
import org.fxyz3d.physics.api.PhysicsWorld;
import org.fxyz3d.physics.model.PhysicsWorldConfiguration;

/**
 * Orekit-oriented backend implementation.
 */
public final class OrekitBackend implements PhysicsBackend {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            false,
            true,
            false,
            false,
            false);

    @Override
    public String id() {
        return "orekit";
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public PhysicsWorld createWorld(PhysicsWorldConfiguration configuration) {
        return new OrekitWorld(configuration);
    }
}
