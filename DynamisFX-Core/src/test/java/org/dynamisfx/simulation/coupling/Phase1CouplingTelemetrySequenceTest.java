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
