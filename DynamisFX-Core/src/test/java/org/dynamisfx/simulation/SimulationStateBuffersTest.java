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

package org.dynamisfx.simulation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dynamisfx.simulation.orbital.OrbitalStateBuffer;
import org.dynamisfx.simulation.rigid.RigidStateBuffer;
import org.junit.jupiter.api.Test;

class SimulationStateBuffersTest {

    @Test
    void createsDefaultBuffers() {
        SimulationStateBuffers buffers = new SimulationStateBuffers();

        assertNotNull(buffers.orbital());
        assertNotNull(buffers.rigid());
    }

    @Test
    void wrapsProvidedBuffers() {
        OrbitalStateBuffer orbital = new OrbitalStateBuffer();
        RigidStateBuffer rigid = new RigidStateBuffer();

        SimulationStateBuffers buffers = new SimulationStateBuffers(orbital, rigid);

        assertSame(orbital, buffers.orbital());
        assertSame(rigid, buffers.rigid());
    }

    @Test
    void validatesConstructorInputs() {
        assertThrows(NullPointerException.class, () -> new SimulationStateBuffers(null, new RigidStateBuffer()));
        assertThrows(NullPointerException.class, () -> new SimulationStateBuffers(new OrbitalStateBuffer(), null));
    }
}
