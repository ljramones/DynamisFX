package org.dynamisfx.samples.utilities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.anyOf;

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

    private static void withBackendProperty(String value, Runnable body) {
        String key = RigidBodyBackendSelector.BACKEND_PROPERTY;
        String prior = System.getProperty(key);
        System.setProperty(key, value);
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
