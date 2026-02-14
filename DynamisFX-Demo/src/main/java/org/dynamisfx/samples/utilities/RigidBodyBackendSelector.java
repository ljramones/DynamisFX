/*
 * Copyright 2024-2026 DynamisFX Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public final class RigidBodyBackendSelector {
    private static final Logger LOG = Logger.getLogger(RigidBodyBackendSelector.class.getName());

    static final String BACKEND_PROPERTY = "dynamisfx.samples.physics.backend";
    static final String BACKEND_ODE4J = "ode4j";
    static final String BACKEND_JOLT = "jolt";
    static final String FORCE_JOLT_FAILURE_PROPERTY = "dynamisfx.samples.physics.forceJoltFailure";
    static final String DIAG_REQUESTED_PROPERTY = "dynamisfx.samples.physics.resolution.requested";
    static final String DIAG_RESOLVED_PROPERTY = "dynamisfx.samples.physics.resolution.resolved";
    static final String DIAG_FALLBACK_USED_PROPERTY = "dynamisfx.samples.physics.resolution.fallbackUsed";
    static final String DIAG_FALLBACK_REASON_PROPERTY = "dynamisfx.samples.physics.resolution.fallbackReason";
    private static volatile BackendSelection lastSelection =
            new BackendSelection(BACKEND_ODE4J, BACKEND_ODE4J, false, "default");

    private RigidBodyBackendSelector() {
    }

    public static PhysicsBackend createBackend() {
        String configured = System.getProperty(BACKEND_PROPERTY, BACKEND_ODE4J).trim().toLowerCase();
        PhysicsBackend backend = switch (configured) {
            case BACKEND_ODE4J -> createOde4j(configured);
            case BACKEND_JOLT -> createJoltOrFallbackToOde4j(configured);
            default -> throw new IllegalArgumentException(
                    "Unsupported backend '" + configured + "'. Use '" + BACKEND_ODE4J + "' or '" + BACKEND_JOLT + "'.");
        };
        BackendSelection selection = lastSelection;
        publishSelection(selection);
        LOG.info(() -> "Rigid backend selection requested="
                + selection.requested()
                + ", resolved="
                + selection.resolved()
                + ", fallback="
                + selection.fallbackUsed()
                + (selection.fallbackReason() == null ? "" : ", reason=" + selection.fallbackReason()));
        return backend;
    }

    public static RigidBodyWorld createRigidWorld(PhysicsWorldConfiguration configuration) {
        return new BackendRigidBodyWorldAdapter(createBackend(), configuration);
    }

    public static BackendSelection selectionSnapshot() {
        return lastSelection;
    }

    private static PhysicsBackend createJoltOrFallbackToOde4j(String configured) {
        if (Boolean.getBoolean(FORCE_JOLT_FAILURE_PROPERTY)) {
            LOG.warning("Forced Jolt failure property is enabled; falling back to ODE4j.");
            return createOde4jFallback(configured, "forced-failure-property");
        }
        try {
            PhysicsBackend jolt = new JoltBackendFactory().createBackend();
            if (jolt != null) {
                lastSelection = new BackendSelection(configured, BACKEND_JOLT, false, null);
                return jolt;
            }
            LOG.warning("Jolt backend returned null; falling back to ODE4j.");
            return createOde4jFallback(configured, "jolt-returned-null");
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Failed to initialize Jolt backend; falling back to ODE4j.", ex);
            return createOde4jFallback(configured, ex.getClass().getSimpleName());
        }
    }

    private static PhysicsBackend createOde4j(String configured) {
        PhysicsBackend backend = requireBackend(new Ode4jBackendFactory().createBackend(), BACKEND_ODE4J);
        lastSelection = new BackendSelection(configured, BACKEND_ODE4J, false, null);
        return backend;
    }

    private static PhysicsBackend createOde4jFallback(String configured, String reason) {
        PhysicsBackend backend = requireBackend(new Ode4jBackendFactory().createBackend(), BACKEND_ODE4J);
        lastSelection = new BackendSelection(configured, BACKEND_ODE4J, true, reason);
        return backend;
    }

    private static PhysicsBackend requireBackend(PhysicsBackend backend, String backendName) {
        if (backend == null) {
            throw new IllegalStateException("Backend factory returned null for '" + backendName + "'.");
        }
        return backend;
    }

    private static void publishSelection(BackendSelection selection) {
        System.setProperty(DIAG_REQUESTED_PROPERTY, selection.requested());
        System.setProperty(DIAG_RESOLVED_PROPERTY, selection.resolved());
        System.setProperty(DIAG_FALLBACK_USED_PROPERTY, Boolean.toString(selection.fallbackUsed()));
        if (selection.fallbackReason() == null) {
            System.clearProperty(DIAG_FALLBACK_REASON_PROPERTY);
        } else {
            System.setProperty(DIAG_FALLBACK_REASON_PROPERTY, selection.fallbackReason());
        }
    }

    public record BackendSelection(String requested, String resolved, boolean fallbackUsed, String fallbackReason) {
    }
}
