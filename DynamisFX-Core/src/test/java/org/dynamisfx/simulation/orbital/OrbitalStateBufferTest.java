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

package org.dynamisfx.simulation.orbital;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;

class OrbitalStateBufferTest {

    @Test
    void storesAndSnapshotsStates() {
        OrbitalStateBuffer buffer = new OrbitalStateBuffer();
        OrbitalState state = new OrbitalState(
                new PhysicsVector3(1.0, 2.0, 3.0),
                new PhysicsVector3(4.0, 5.0, 6.0),
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                7.0);

        buffer.put("lander-1", state);

        assertEquals(state, buffer.get("lander-1").orElseThrow());
        assertEquals(state, buffer.snapshot().get("lander-1"));
        assertTrue(buffer.remove("lander-1"));
        assertFalse(buffer.remove("lander-1"));
    }

    @Test
    void validatesInputs() {
        OrbitalStateBuffer buffer = new OrbitalStateBuffer();
        OrbitalState state = new OrbitalState(
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                0.0);

        assertThrows(IllegalArgumentException.class, () -> buffer.put("", state));
        assertThrows(IllegalArgumentException.class, () -> buffer.put("ok", null));
        assertThrows(IllegalArgumentException.class, () -> buffer.get(" "));
        assertThrows(IllegalArgumentException.class, () -> buffer.remove(null));
    }
}
