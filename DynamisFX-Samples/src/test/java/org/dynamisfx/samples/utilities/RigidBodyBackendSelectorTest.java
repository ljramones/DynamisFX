package org.dynamisfx.samples.utilities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.junit.jupiter.api.Test;

class RigidBodyBackendSelectorTest {

    @Test
    void ode4jSelectionProducesBackend() {
        withBackendProperty("ode4j", () -> {
            PhysicsBackend backend = RigidBodyBackendSelector.createBackend();
            try {
                assertThat(backend, notNullValue());
                assertThat(backend.id(), is("ode4j"));
            } finally {
                backend.close();
            }
        });
    }

    @Test
    void joltSelectionReturnsJoltOrFallsBackToOde4j() {
        withBackendProperty("jolt", () -> {
            PhysicsBackend backend = RigidBodyBackendSelector.createBackend();
            try {
                assertThat(backend, notNullValue());
                assertThat(backend.id(), anyOf(is("jolt"), is("ode4j")));
            } finally {
                backend.close();
            }
        });
    }

    @Test
    void joltForcedFailureFallsBackToOde4j() {
        withBackendProperty("jolt", () ->
                withSystemProperty(RigidBodyBackendSelector.FORCE_JOLT_FAILURE_PROPERTY, "true", () -> {
                    PhysicsBackend backend = RigidBodyBackendSelector.createBackend();
                    try {
                        assertThat(backend, notNullValue());
                        assertThat(backend.id(), is("ode4j"));
                        RigidBodyBackendSelector.BackendSelection selection =
                                RigidBodyBackendSelector.selectionSnapshot();
                        assertThat(selection.fallbackUsed(), is(true));
                        assertThat(selection.fallbackReason(), containsString("forced"));
                    } finally {
                        backend.close();
                    }
                }));
    }

    private static void withBackendProperty(String value, Runnable body) {
        withSystemProperty(RigidBodyBackendSelector.BACKEND_PROPERTY, value, body);
    }

    private static void withSystemProperty(String key, String value, Runnable body) {
        String prior = System.getProperty(key);
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
        try {
            body.run();
        } finally {
            if (prior == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, prior);
            }
        }
    }
}
