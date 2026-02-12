package org.fxyz3d.physics.orekit;

import org.fxyz3d.physics.api.PhysicsBackend;
import org.fxyz3d.physics.api.PhysicsBackendFactory;

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
