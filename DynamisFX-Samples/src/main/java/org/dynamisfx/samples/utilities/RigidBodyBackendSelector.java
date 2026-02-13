package org.dynamisfx.samples.utilities;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.jolt.JoltBackendFactory;
import org.dynamisfx.physics.ode4j.Ode4jBackendFactory;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;

/**
 * Selects rigid-body backend for samples using a simple runtime property.
 */
final class RigidBodyBackendSelector {
    private static final Logger LOG = Logger.getLogger(RigidBodyBackendSelector.class.getName());

    static final String BACKEND_PROPERTY = "dynamisfx.samples.physics.backend";
    static final String BACKEND_ODE4J = "ode4j";
    static final String BACKEND_JOLT = "jolt";

    private RigidBodyBackendSelector() {
    }

    static PhysicsBackend createBackend() {
        String configured = System.getProperty(BACKEND_PROPERTY, BACKEND_ODE4J).trim().toLowerCase();
        return switch (configured) {
            case BACKEND_ODE4J -> requireBackend(new Ode4jBackendFactory().createBackend(), BACKEND_ODE4J);
            case BACKEND_JOLT -> createJoltOrFallbackToOde4j();
            default -> throw new IllegalArgumentException(
                    "Unsupported backend '" + configured + "'. Use '" + BACKEND_ODE4J + "' or '" + BACKEND_JOLT + "'.");
        };
    }

    static RigidBodyWorld createRigidWorld(PhysicsWorldConfiguration configuration) {
        return new BackendRigidBodyWorldAdapter(createBackend(), configuration);
    }

    private static PhysicsBackend createJoltOrFallbackToOde4j() {
        try {
            PhysicsBackend jolt = new JoltBackendFactory().createBackend();
            if (jolt != null) {
                return jolt;
            }
            LOG.warning("Jolt backend returned null; falling back to ODE4j.");
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Failed to initialize Jolt backend; falling back to ODE4j.", ex);
        }
        return requireBackend(new Ode4jBackendFactory().createBackend(), BACKEND_ODE4J);
    }

    private static PhysicsBackend requireBackend(PhysicsBackend backend, String backendName) {
        if (backend == null) {
            throw new IllegalStateException("Backend factory returned null for '" + backendName + "'.");
        }
        return backend;
    }
}
