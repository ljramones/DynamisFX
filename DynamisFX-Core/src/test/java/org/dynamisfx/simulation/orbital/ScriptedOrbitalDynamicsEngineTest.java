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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;

class ScriptedOrbitalDynamicsEngineTest {

    @Test
    void propagatesConfiguredTrajectory() {
        ScriptedOrbitalDynamicsEngine engine = new ScriptedOrbitalDynamicsEngine();
        engine.setTrajectory("lander-1", (time, frame) -> new OrbitalState(
                new PhysicsVector3(time, 0, 0),
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                frame,
                time));

        Map<String, OrbitalState> states = engine.propagateTo(List.of("lander-1"), 12.5, ReferenceFrame.WORLD);

        assertEquals(1, states.size());
        assertEquals(12.5, states.get("lander-1").position().x(), 1e-9);
    }

    @Test
    void validatesInputs() {
        ScriptedOrbitalDynamicsEngine engine = new ScriptedOrbitalDynamicsEngine();
        assertThrows(IllegalArgumentException.class, () -> engine.setTrajectory("", (time, frame) -> null));
        assertThrows(NullPointerException.class, () -> engine.setTrajectory("id", null));
        assertThrows(IllegalArgumentException.class, () -> engine.propagateTo(null, 1.0, ReferenceFrame.WORLD));
        assertThrows(IllegalArgumentException.class, () -> engine.propagateTo(List.of("id"), Double.NaN, ReferenceFrame.WORLD));
        assertThrows(NullPointerException.class, () -> engine.propagateTo(List.of("id"), 1.0, null));

        assertTrue(engine.propagateTo(List.of("missing"), 1.0, ReferenceFrame.WORLD).isEmpty());
    }
}
