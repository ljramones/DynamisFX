package org.fxyz3d.physics.orekit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fxyz3d.physics.model.PhysicsBodyState;
import org.fxyz3d.physics.model.PhysicsQuaternion;
import org.fxyz3d.physics.model.PhysicsVector3;
import org.fxyz3d.physics.model.ReferenceFrame;
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
}
