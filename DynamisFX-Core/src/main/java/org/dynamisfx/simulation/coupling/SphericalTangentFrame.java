package org.dynamisfx.simulation.coupling;

import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Spherical-body local tangent frame pose (x=east, y=north, z=up) in global coordinates.
 */
public record SphericalTangentFrame(
        PhysicsVector3 anchorPosition,
        PhysicsQuaternion anchorOrientation,
        double latitudeRadians,
        double longitudeRadians,
        double altitudeMeters) {
}
