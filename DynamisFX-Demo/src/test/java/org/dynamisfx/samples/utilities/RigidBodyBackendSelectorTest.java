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
