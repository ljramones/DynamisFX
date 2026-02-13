package org.dynamisfx.simulation.coupling;

import java.util.Objects;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.orbital.OrbitalState;

/**
 * Coordinate conversions between global orbital state and zone-local rigid state.
 */
public final class ZoneFrameTransform {

    private ZoneFrameTransform() {
    }

    public static PhysicsVector3 globalToLocalPosition(PhysicsVector3 globalPosition, PhysicsZone zone) {
        Objects.requireNonNull(globalPosition, "globalPosition must not be null");
        validateZone(zone);
        return new PhysicsVector3(
                globalPosition.x() - zone.anchorPosition().x(),
                globalPosition.y() - zone.anchorPosition().y(),
                globalPosition.z() - zone.anchorPosition().z());
    }

    public static PhysicsVector3 localToGlobalPosition(PhysicsVector3 localPosition, PhysicsZone zone) {
        Objects.requireNonNull(localPosition, "localPosition must not be null");
        validateZone(zone);
        return new PhysicsVector3(
                localPosition.x() + zone.anchorPosition().x(),
                localPosition.y() + zone.anchorPosition().y(),
                localPosition.z() + zone.anchorPosition().z());
    }

    public static PhysicsBodyState orbitalToLocalRigid(
            OrbitalState orbitalState,
            double simulationTimeSeconds,
            PhysicsZone zone) {
        Objects.requireNonNull(orbitalState, "orbitalState must not be null");
        validateZone(zone);
        validateFramesCompatible(orbitalState.referenceFrame(), zone.anchorFrame(), "orbitalState.referenceFrame");
        return new PhysicsBodyState(
                globalToLocalPosition(orbitalState.position(), zone),
                orbitalState.orientation(),
                orbitalState.linearVelocity(),
                PhysicsVector3.ZERO,
                zone.anchorFrame(),
                simulationTimeSeconds);
    }

    public static OrbitalState localRigidToOrbital(
            PhysicsBodyState localRigidState,
            double simulationTimeSeconds,
            PhysicsZone zone) {
        Objects.requireNonNull(localRigidState, "localRigidState must not be null");
        validateZone(zone);
        validateFramesCompatible(localRigidState.referenceFrame(), zone.anchorFrame(), "localRigidState.referenceFrame");
        return new OrbitalState(
                localToGlobalPosition(localRigidState.position(), zone),
                localRigidState.linearVelocity(),
                localRigidState.orientation(),
                zone.anchorFrame(),
                simulationTimeSeconds);
    }

    private static void validateZone(PhysicsZone zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        Objects.requireNonNull(zone.anchorPosition(), "zone.anchorPosition must not be null");
        Objects.requireNonNull(zone.anchorFrame(), "zone.anchorFrame must not be null");
        if (zone.anchorFrame() == ReferenceFrame.UNSPECIFIED) {
            throw new IllegalArgumentException("zone.anchorFrame must not be UNSPECIFIED");
        }
    }

    private static void validateFramesCompatible(ReferenceFrame source, ReferenceFrame target, String sourceName) {
        Objects.requireNonNull(source, sourceName + " must not be null");
        Objects.requireNonNull(target, "target frame must not be null");
        if (source == ReferenceFrame.UNSPECIFIED) {
            throw new IllegalArgumentException(sourceName + " must not be UNSPECIFIED");
        }
        if (source != target) {
            throw new IllegalArgumentException(sourceName + " must match zone.anchorFrame");
        }
    }
}
