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

package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.junit.jupiter.api.Test;

class ZoneBodyRegistryTest {

    @Test
    void bindsAndUnbindsObjectBody() {
        ZoneBodyRegistry registry = new ZoneBodyRegistry();

        registry.bind("lander-1", new ZoneId("zone-a"), new PhysicsBodyHandle(7));

        ZoneBodyRegistry.ZoneBodyBinding binding = registry.bindingForObject("lander-1").orElseThrow();
        assertEquals("lander-1", binding.objectId());
        assertEquals("zone-a", binding.zoneId().value());
        assertEquals(7L, binding.bodyHandle().value());
        assertTrue(registry.unbind("lander-1").isPresent());
        assertTrue(registry.bindingForObject("lander-1").isEmpty());
    }

    @Test
    void validatesInputs() {
        ZoneBodyRegistry registry = new ZoneBodyRegistry();

        assertThrows(IllegalArgumentException.class, () -> registry.bind("", new ZoneId("zone-a"), new PhysicsBodyHandle(1)));
        assertThrows(NullPointerException.class, () -> registry.bind("x", null, new PhysicsBodyHandle(1)));
        assertThrows(NullPointerException.class, () -> registry.bind("x", new ZoneId("zone-a"), null));
        assertThrows(IllegalArgumentException.class, () -> registry.bindingForObject(" "));
        assertThrows(IllegalArgumentException.class, () -> registry.unbind(null));
    }
}
