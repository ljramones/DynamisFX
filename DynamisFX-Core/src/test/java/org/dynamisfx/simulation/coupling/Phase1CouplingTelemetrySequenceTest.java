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

import java.util.ArrayList;
import java.util.List;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class Phase1CouplingTelemetrySequenceTest {

    @Test
    void emitsPromoteThenBlockedDemoteThenDemoteAfterContactClears() {
        MutableCouplingObservationProvider observations = new MutableCouplingObservationProvider();
        DefaultCouplingManager manager = Phase1CouplingBootstrap.createManager(
                observations,
                100.0,
                200.0,
                0.0);
        manager.registerZone(new StubZone());
        manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);

        List<CouplingTelemetryEvent> events = new ArrayList<>();
        manager.addTelemetryListener(events::add);

        observations.setDistanceMeters("lander-1", 50.0);
        observations.setActiveContact("lander-1", false);
        manager.update(1.0);

        observations.setDistanceMeters("lander-1", 300.0);
        observations.setActiveContact("lander-1", true);
        manager.update(2.0);

        observations.setActiveContact("lander-1", false);
        manager.update(3.0);

        assertEquals(3, events.size());
        assertEquals(CouplingDecisionReason.PROMOTE_DISTANCE_THRESHOLD, events.get(0).reason());
        assertEquals(CouplingDecisionReason.BLOCKED_BY_CONTACT, events.get(1).reason());
        assertEquals(CouplingDecisionReason.DEMOTE_DISTANCE_THRESHOLD, events.get(2).reason());
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, events.get(0).toMode());
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, events.get(1).toMode());
        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, events.get(2).toMode());
        assertEquals("zone-a", events.get(0).selectedZoneId().orElseThrow().value());
        assertEquals(ReferenceFrame.WORLD, events.get(0).selectedZoneFrame().orElseThrow());
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
