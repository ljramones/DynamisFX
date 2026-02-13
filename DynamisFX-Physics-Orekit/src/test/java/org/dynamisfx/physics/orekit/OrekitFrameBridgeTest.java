package org.dynamisfx.physics.orekit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;
import org.orekit.time.AbsoluteDate;

class OrekitFrameBridgeTest {

    @Test
    void normalizesUnspecifiedStateFrameToTarget() {
        PhysicsBodyState state = new PhysicsBodyState(
                new PhysicsVector3(1.0, 2.0, 3.0),
                PhysicsQuaternion.IDENTITY,
                new PhysicsVector3(4.0, 5.0, 6.0),
                PhysicsVector3.ZERO,
                ReferenceFrame.UNSPECIFIED,
                0.0);

        PhysicsBodyState normalized = OrekitFrameBridge.transformState(
                state,
                ReferenceFrame.EME2000,
                AbsoluteDate.J2000_EPOCH);

        assertEquals(ReferenceFrame.EME2000, normalized.referenceFrame());
        assertEquals(1.0, normalized.position().x(), 1e-9);
        assertEquals(5.0, normalized.linearVelocity().y(), 1e-9);
    }

    @Test
    void roundTripsBetweenEme2000AndIcrfWithLowError() {
        PhysicsBodyState eme = new PhysicsBodyState(
                new PhysicsVector3(7_000_000.0, 1200.0, -300.0),
                PhysicsQuaternion.IDENTITY,
                new PhysicsVector3(-120.0, 7_500.0, 4.0),
                PhysicsVector3.ZERO,
                ReferenceFrame.EME2000,
                0.0);

        PhysicsBodyState icrf = OrekitFrameBridge.transformState(
                eme, ReferenceFrame.ICRF, AbsoluteDate.J2000_EPOCH);
        PhysicsBodyState roundTrip = OrekitFrameBridge.transformState(
                icrf, ReferenceFrame.EME2000, AbsoluteDate.J2000_EPOCH);

        double posErr = distance(eme.position(), roundTrip.position());
        double velErr = distance(eme.linearVelocity(), roundTrip.linearVelocity());
        assertTrue(posErr < 1e-3);
        assertTrue(velErr < 1e-6);
    }

    private static double distance(PhysicsVector3 a, PhysicsVector3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
