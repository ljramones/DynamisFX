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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.entity.SimulationEntityRegistry;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.junit.jupiter.api.Test;

class SimulationTransformBridgeTest {

    @Test
    void writesOrbitalAndRigidStatesIntoStore() {
        SimulationEntityRegistry<String> registry = new SimulationEntityRegistry<>();
        registry.register("a", "entity-a");
        TransformStore store = new TransformStore(1);
        SimulationTransformBridge bridge = new SimulationTransformBridge(registry, store);

        bridge.writeOrbitalStates(Map.of("a", new OrbitalState(
                new PhysicsVector3(1, 2, 3),
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                0.0)));
        bridge.publish(1.0);
        TransformStore.TransformSample sample = store.sample(0);
        assertEquals(1.0, sample.posX(), 1e-9);

        bridge.writeRigidStates(Map.of("a", new PhysicsBodyState(
                new PhysicsVector3(4, 5, 6),
                PhysicsQuaternion.IDENTITY,
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                ReferenceFrame.WORLD,
                2.0)));
        bridge.publish(2.0);
        sample = store.sample(0);
        assertEquals(4.0, sample.posX(), 1e-9);
        assertEquals(2.0, sample.simulationTimeSeconds(), 1e-9);
    }
}
