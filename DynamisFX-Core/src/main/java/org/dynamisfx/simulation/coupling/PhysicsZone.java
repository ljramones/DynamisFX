package org.dynamisfx.simulation.coupling;

import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;

/**
 * Local rigid-body simulation bubble anchored to a global frame.
 */
public interface PhysicsZone {

    ZoneId zoneId();

    ReferenceFrame anchorFrame();

    PhysicsVector3 anchorPosition();

    double radiusMeters();

    RigidBodyWorld world();
}
