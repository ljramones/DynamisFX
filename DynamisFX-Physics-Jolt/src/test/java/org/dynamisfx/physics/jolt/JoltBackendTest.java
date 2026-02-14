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

package org.dynamisfx.physics.jolt;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class JoltBackendTest {

    @AfterEach
    void clearProviderProperty() {
        System.clearProperty(JoltBackend.PROVIDER_PROPERTY);
    }

    @Test
    void failsFastWhenNativeBridgeIsUnavailable() {
        JoltBackend backend = new JoltBackend(new JoltNativeBridge(false));
        PhysicsWorldConfiguration configuration = new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                1.0 / 120.0);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> backend.createWorld(configuration));
        assertTrue(error.getMessage().contains("dynamisfx_jolt_cshim"));
    }

    @Test
    void rejectsUnknownProviderProperty() {
        System.setProperty(JoltBackend.PROVIDER_PROPERTY, "nope");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, JoltBackend::new);
        assertTrue(error.getMessage().contains("Unknown jolt provider"));
    }
}
