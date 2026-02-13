package org.dynamisfx.simulation.coupling;

import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;

/**
 * Local rigid-body simulation bubble anchored to a global frame.
 */
public interface PhysicsZone {

    ZoneId zoneId();

    ReferenceFrame anchorFrame();

    PhysicsVector3 anchorPosition();

    /**
     * Zone-local orientation in the anchor frame (identity means axis-aligned local frame).
     */
    default PhysicsQuaternion anchorOrientation() {
        return PhysicsQuaternion.IDENTITY;
    }

    double radiusMeters();

    RigidBodyWorld world();
}
