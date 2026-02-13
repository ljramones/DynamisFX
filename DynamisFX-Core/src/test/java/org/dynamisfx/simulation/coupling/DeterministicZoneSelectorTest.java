package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class DeterministicZoneSelectorTest {

    @Test
    void prefersBoundZoneWhenPresent() {
        PhysicsZone zoneA = new StubZone("zone-a", new PhysicsVector3(0.0, 0.0, 0.0));
        PhysicsZone zoneB = new StubZone("zone-b", new PhysicsVector3(100.0, 0.0, 0.0));
        PhysicsZone selected = DeterministicZoneSelector
                .select(List.of(zoneA, zoneB), new ZoneId("zone-b"), null)
                .orElseThrow();
        assertEquals("zone-b", selected.zoneId().value());
    }

    @Test
    void selectsNearestZoneWithStableTieBreak() {
        PhysicsZone zoneB = new StubZone("zone-b", new PhysicsVector3(10.0, 0.0, 0.0));
        PhysicsZone zoneA = new StubZone("zone-a", new PhysicsVector3(-10.0, 0.0, 0.0));
        PhysicsZone selected = DeterministicZoneSelector
                .select(List.of(zoneB, zoneA), null, PhysicsVector3.ZERO)
                .orElseThrow();
        assertEquals("zone-a", selected.zoneId().value());
    }

    @Test
    void fallsBackToLexicographicOrderWithoutHints() {
        PhysicsZone zoneC = new StubZone("zone-c", new PhysicsVector3(30.0, 0.0, 0.0));
        PhysicsZone zoneA = new StubZone("zone-a", new PhysicsVector3(10.0, 0.0, 0.0));
        PhysicsZone selected = DeterministicZoneSelector
                .select(List.of(zoneC, zoneA), null, null)
                .orElseThrow();
        assertEquals("zone-a", selected.zoneId().value());
    }

    private record StubZone(String id, PhysicsVector3 anchorPosition) implements PhysicsZone {

        @Override
        public ZoneId zoneId() {
            return new ZoneId(id);
        }

        @Override
        public ReferenceFrame anchorFrame() {
            return ReferenceFrame.WORLD;
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
