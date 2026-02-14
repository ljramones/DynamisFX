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

package org.dynamisfx.physics.orekit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.junit.jupiter.api.Test;

class OrekitBackendFactoryTest {

    @Test
    void createsBackendWithExpectedCapabilities() {
        OrekitBackendFactory factory = new OrekitBackendFactory();
        assertEquals("orekit", factory.backendId());

        PhysicsBackend backend = factory.createBackend();
        assertEquals("orekit", backend.id());
        assertFalse(backend.capabilities().supportsRigidBodies());
        assertTrue(backend.capabilities().supportsNBody());
        assertFalse(backend.capabilities().supportsJoints());
    }
}
