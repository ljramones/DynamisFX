package org.dynamisfx.samples.utilities;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.jolt.JoltBackendFactory;
import org.dynamisfx.physics.ode4j.Ode4jBackendFactory;

/**
 * Selects rigid-body backend for samples using a simple runtime property.
 */
final class RigidBodyBackendSelector {

    static final String BACKEND_PROPERTY = "dynamisfx.samples.physics.backend";
    static final String BACKEND_ODE4J = "ode4j";
    static final String BACKEND_JOLT = "jolt";

    private RigidBodyBackendSelector() {
    }

    static PhysicsBackend createBackend() {
        String configured = System.getProperty(BACKEND_PROPERTY, BACKEND_ODE4J).trim().toLowerCase();
        return switch (configured) {
            case BACKEND_ODE4J -> new Ode4jBackendFactory().createBackend();
            case BACKEND_JOLT -> new JoltBackendFactory().createBackend();
            default -> throw new IllegalArgumentException(
                    "Unsupported backend '" + configured + "'. Use '" + BACKEND_ODE4J + "' or '" + BACKEND_JOLT + "'.");
        };
    }
}
