package org.dynamisfx.physics.orekit;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsBackendFactory;

/**
 * Phase-3 kickoff factory for the Orekit-oriented backend module.
 */
public final class OrekitBackendFactory implements PhysicsBackendFactory {

    @Override
    public String backendId() {
        return "orekit";
    }

    @Override
    public PhysicsBackend createBackend() {
        return new OrekitBackend();
    }
}
