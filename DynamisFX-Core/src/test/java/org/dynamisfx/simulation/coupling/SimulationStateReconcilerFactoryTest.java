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

import java.util.List;
import java.util.Optional;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.SimulationStateBuffers;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class SimulationStateReconcilerFactoryTest {

    @Test
    void createsReconcilerThatPromotesAndDemotesThroughStateBuffers() {
        SimulationStateBuffers buffers = new SimulationStateBuffers();
        CouplingStateReconciler reconciler = SimulationStateReconcilerFactory.create(
                buffers,
                buffers.orbital()::put);
        PhysicsZone zone = new StubZone(new PhysicsVector3(100.0, 0.0, 0.0));

        buffers.orbital().put("lander-1", new OrbitalState(
                new PhysicsVector3(130.0, 0.0, 0.0),
                new PhysicsVector3(2.0, 0.0, 0.0),
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                1.0));
        reconciler.onTransition(new CouplingModeTransitionEvent(
                2.0,
                "lander-1",
                ObjectSimulationMode.ORBITAL_ONLY,
                ObjectSimulationMode.PHYSICS_ACTIVE,
                CouplingDecisionReason.PROMOTE_DISTANCE_THRESHOLD,
                List.of(zone)));

        PhysicsBodyState rigid = buffers.rigid().get("lander-1").orElseThrow();
        assertEquals(30.0, rigid.position().x(), 1e-9);
        assertTrue(buffers.orbital().get("lander-1").isPresent());

        buffers.rigid().put("lander-1", new PhysicsBodyState(
                new PhysicsVector3(10.0, 0.0, 0.0),
                PhysicsQuaternion.IDENTITY,
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                ReferenceFrame.WORLD,
                3.0));
        reconciler.onTransition(new CouplingModeTransitionEvent(
                4.0,
                "lander-1",
                ObjectSimulationMode.PHYSICS_ACTIVE,
                ObjectSimulationMode.ORBITAL_ONLY,
                CouplingDecisionReason.DEMOTE_DISTANCE_THRESHOLD,
                List.of(zone)));

        OrbitalState orbital = buffers.orbital().get("lander-1").orElseThrow();
        assertEquals(110.0, orbital.position().x(), 1e-9);
    }

    @Test
    void validatesInputs() {
        SimulationStateBuffers buffers = new SimulationStateBuffers();
        assertThrows(NullPointerException.class, () -> SimulationStateReconcilerFactory.create(null, buffers.orbital()::put));
        assertThrows(NullPointerException.class, () -> SimulationStateReconcilerFactory.create(buffers, null));
        assertThrows(NullPointerException.class, () -> SimulationStateReconcilerFactory.create(
                buffers, buffers.orbital()::put, null, (id, zones) -> Optional.empty()));
        assertThrows(NullPointerException.class, () -> SimulationStateReconcilerFactory.create(
                buffers, buffers.orbital()::put, id -> {
                }, null));
        assertThrows(NullPointerException.class, () -> SimulationStateReconcilerFactory.create(
                buffers, buffers.orbital()::put, id -> {
                }, (id, zones) -> Optional.empty(), null));
    }

    private record StubZone(PhysicsVector3 anchorPosition) implements PhysicsZone {

        @Override
        public ZoneId zoneId() {
            return new ZoneId("zone-a");
        }

        @Override
        public ReferenceFrame anchorFrame() {
            return ReferenceFrame.WORLD;
        }

        @Override
        public double radiusMeters() {
            return 1_000.0;
        }

        @Override
        public RigidBodyWorld world() {
            return null;
        }
    }
}
