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

package org.dynamisfx.simulation.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimulationEntityRegistryTest {

    @Test
    void registersRetrievesAndRemovesEntities() {
        SimulationEntityRegistry<String> registry = new SimulationEntityRegistry<>();
        registry.register("lander-1", "entity-a");

        assertEquals(1, registry.size());
        assertEquals("entity-a", registry.get("lander-1").orElseThrow());
        assertTrue(registry.remove("lander-1"));
        assertEquals(0, registry.size());
    }

    @Test
    void validatesInputs() {
        SimulationEntityRegistry<String> registry = new SimulationEntityRegistry<>();
        assertThrows(IllegalArgumentException.class, () -> registry.register("", "x"));
        assertThrows(NullPointerException.class, () -> registry.register("id", null));
        assertThrows(IllegalArgumentException.class, () -> registry.get(" "));
        assertThrows(IllegalArgumentException.class, () -> registry.remove(""));
    }
}
