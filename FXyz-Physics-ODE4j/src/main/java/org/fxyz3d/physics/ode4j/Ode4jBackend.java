package org.fxyz3d.physics.ode4j;

import org.fxyz3d.physics.api.PhysicsBackend;
import org.fxyz3d.physics.api.PhysicsCapabilities;
import org.fxyz3d.physics.api.PhysicsWorld;
import org.fxyz3d.physics.model.PhysicsWorldConfiguration;
import org.ode4j.ode.OdeHelper;

/**
 * ODE4j backend implementation.
 */
public final class Ode4jBackend implements PhysicsBackend {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            true,
            false,
            false,
            false,
            false);

    @Override
    public String id() {
        return "ode4j";
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public PhysicsWorld createWorld(PhysicsWorldConfiguration configuration) {
        OdeHelper.initODE();
        return new Ode4jWorld(configuration);
    }
}
