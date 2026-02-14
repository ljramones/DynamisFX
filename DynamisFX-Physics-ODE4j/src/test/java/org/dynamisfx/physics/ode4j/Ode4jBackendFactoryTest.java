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

package org.dynamisfx.physics.ode4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.junit.jupiter.api.Test;

class Ode4jBackendFactoryTest {

    @Test
    void createsBackendWithExpectedIdentityAndCapabilities() {
        Ode4jBackendFactory factory = new Ode4jBackendFactory();
        assertEquals("ode4j", factory.backendId());

        PhysicsBackend backend = factory.createBackend();
        assertNotNull(backend);
        assertEquals("ode4j", backend.id());
        assertTrue(backend.capabilities().supportsRigidBodies());
    }
}
