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

import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class Phase1CouplingBootstrapTest {

    @Test
    void defaultManagerPromotesAndDemotesUsingObservationProvider() {
        MutableCouplingObservationProvider observationProvider = new MutableCouplingObservationProvider();
        DefaultCouplingManager manager = Phase1CouplingBootstrap.createDefaultManager(observationProvider);
        manager.registerZone(new StubZone());
        manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);

        observationProvider.setDistanceMeters("lander-1", 200.0);
        manager.update(1.0);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());

        observationProvider.setDistanceMeters("lander-1", 2_000.0);
        observationProvider.setActiveContact("lander-1", true);
        manager.update(3.0);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());

        observationProvider.setActiveContact("lander-1", false);
        manager.update(4.0);
        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, manager.modeFor("lander-1").orElseThrow());
    }

    @Test
    void customThresholdManagerUsesProvidedThresholds() {
        MutableCouplingObservationProvider observationProvider = new MutableCouplingObservationProvider();
        DefaultCouplingManager manager = Phase1CouplingBootstrap.createManager(
                observationProvider,
                10.0,
                20.0,
                0.0);
        manager.registerZone(new StubZone());
        manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);

        observationProvider.setDistanceMeters("lander-1", 15.0);
        manager.update(1.0);
        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, manager.modeFor("lander-1").orElseThrow());

        observationProvider.setDistanceMeters("lander-1", 9.0);
        manager.update(2.0);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());
    }

    private static final class StubZone implements PhysicsZone {
        @Override
        public ZoneId zoneId() {
            return new ZoneId("zone-a");
        }

        @Override
        public ReferenceFrame anchorFrame() {
            return ReferenceFrame.WORLD;
        }

        @Override
        public PhysicsVector3 anchorPosition() {
            return PhysicsVector3.ZERO;
        }

        @Override
        public double radiusMeters() {
            return 1000.0;
        }

        @Override
        public RigidBodyWorld world() {
            return null;
        }
    }
}
