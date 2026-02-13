package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class ZoneFrameTransformTest {

    @Test
    void convertsOrbitalToLocalAndBack() {
        PhysicsZone zone = new StubZone(new ZoneId("zone-a"), new PhysicsVector3(100.0, -5.0, 20.0), ReferenceFrame.WORLD);
        OrbitalState orbital = new OrbitalState(
                new PhysicsVector3(112.0, -2.0, 26.0),
                new PhysicsVector3(1.0, 2.0, 3.0),
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                1.0);

        PhysicsBodyState local = ZoneFrameTransform.orbitalToLocalRigid(orbital, 2.0, zone);
        assertEquals(12.0, local.position().x(), 1e-9);
        assertEquals(3.0, local.position().y(), 1e-9);
        assertEquals(6.0, local.position().z(), 1e-9);

        OrbitalState restored = ZoneFrameTransform.localRigidToOrbital(local, 3.0, zone);
        assertEquals(orbital.position().x(), restored.position().x(), 1e-9);
        assertEquals(orbital.position().y(), restored.position().y(), 1e-9);
        assertEquals(orbital.position().z(), restored.position().z(), 1e-9);
    }

    @Test
    void rejectsMismatchedFrames() {
        PhysicsZone zone = new StubZone(new ZoneId("zone-a"), PhysicsVector3.ZERO, ReferenceFrame.WORLD);
        OrbitalState orbital = new OrbitalState(
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.ECEF,
                1.0);

        assertThrows(IllegalArgumentException.class, () -> ZoneFrameTransform.orbitalToLocalRigid(orbital, 1.0, zone));
    }

    @Test
    void rejectsUnspecifiedInputFrame() {
        PhysicsZone zone = new StubZone(new ZoneId("zone-a"), PhysicsVector3.ZERO, ReferenceFrame.WORLD);
        PhysicsBodyState local = new PhysicsBodyState(
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                ReferenceFrame.UNSPECIFIED,
                1.0);
        assertThrows(IllegalArgumentException.class, () -> ZoneFrameTransform.localRigidToOrbital(local, 2.0, zone));
    }

    private record StubZone(ZoneId zoneId, PhysicsVector3 anchorPosition, ReferenceFrame anchorFrame) implements PhysicsZone {

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
