package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class ZoneGravityProjectionTest {

    @Test
    void projectsSphericalGravityIntoLocalFrame() {
        SphericalTangentFrame frame = SphericalTangentFrameBuilder.fromGeodetic(0.0, 0.0, 0.0, 1000.0);
        PhysicsZone zone = new StubZone(frame.anchorPosition(), frame.anchorOrientation());
        PhysicsVector3 gravity = ZoneGravityProjection.projectSphericalGravity(zone, 9.81);
        assertEquals(0.0, gravity.x(), 1e-9);
        assertEquals(0.0, gravity.y(), 1e-9);
        assertEquals(-9.81, gravity.z(), 1e-9);
    }

    private record StubZone(PhysicsVector3 anchorPosition, PhysicsQuaternion anchorOrientation) implements PhysicsZone {

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
            return 1000.0;
        }

        @Override
        public RigidBodyWorld world() {
            return null;
        }
    }
}
