package org.dynamisfx.physics.ode4j;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsBackendFactory;

/**
 * Phase-2 kickoff factory for the ODE4j backend module.
 */
public final class Ode4jBackendFactory implements PhysicsBackendFactory {

    @Override
    public String backendId() {
        return "ode4j";
    }

    @Override
    public PhysicsBackend createBackend() {
        return new Ode4jBackend();
    }
}
