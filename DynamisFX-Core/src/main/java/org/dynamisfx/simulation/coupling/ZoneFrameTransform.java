package org.dynamisfx.simulation.coupling;

import java.util.Objects;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
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
        PhysicsVector3 translated = subtract(globalPosition, zone.anchorPosition());
        return rotate(inverseNormalized(zone.anchorOrientation()), translated);
    }

    public static PhysicsVector3 localToGlobalPosition(PhysicsVector3 localPosition, PhysicsZone zone) {
        Objects.requireNonNull(localPosition, "localPosition must not be null");
        validateZone(zone);
        PhysicsVector3 rotated = rotate(normalized(zone.anchorOrientation()), localPosition);
        return add(rotated, zone.anchorPosition());
    }

    public static PhysicsBodyState orbitalToLocalRigid(
            OrbitalState orbitalState,
            double simulationTimeSeconds,
            PhysicsZone zone) {
        Objects.requireNonNull(orbitalState, "orbitalState must not be null");
        validateZone(zone);
        validateFramesCompatible(orbitalState.referenceFrame(), zone.anchorFrame(), "orbitalState.referenceFrame");
        PhysicsQuaternion inverseAnchor = inverseNormalized(zone.anchorOrientation());
        return new PhysicsBodyState(
                globalToLocalPosition(orbitalState.position(), zone),
                multiply(inverseAnchor, orbitalState.orientation()),
                rotate(inverseAnchor, orbitalState.linearVelocity()),
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
        PhysicsQuaternion anchor = normalized(zone.anchorOrientation());
        return new OrbitalState(
                localToGlobalPosition(localRigidState.position(), zone),
                rotate(anchor, localRigidState.linearVelocity()),
                multiply(anchor, localRigidState.orientation()),
                zone.anchorFrame(),
                simulationTimeSeconds);
    }

    private static void validateZone(PhysicsZone zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        Objects.requireNonNull(zone.anchorPosition(), "zone.anchorPosition must not be null");
        Objects.requireNonNull(zone.anchorFrame(), "zone.anchorFrame must not be null");
        Objects.requireNonNull(zone.anchorOrientation(), "zone.anchorOrientation must not be null");
        if (zone.anchorFrame() == ReferenceFrame.UNSPECIFIED) {
            throw new IllegalArgumentException("zone.anchorFrame must not be UNSPECIFIED");
        }
        if (normSquared(zone.anchorOrientation()) <= 0.0) {
            throw new IllegalArgumentException("zone.anchorOrientation must have non-zero norm");
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

    private static PhysicsVector3 add(PhysicsVector3 a, PhysicsVector3 b) {
        return new PhysicsVector3(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }

    private static PhysicsVector3 subtract(PhysicsVector3 a, PhysicsVector3 b) {
        return new PhysicsVector3(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
    }

    private static PhysicsVector3 rotate(PhysicsQuaternion rotation, PhysicsVector3 vector) {
        PhysicsQuaternion v = new PhysicsQuaternion(vector.x(), vector.y(), vector.z(), 0.0);
        PhysicsQuaternion result = multiply(multiply(rotation, v), conjugate(rotation));
        return new PhysicsVector3(result.x(), result.y(), result.z());
    }

    private static PhysicsQuaternion inverseNormalized(PhysicsQuaternion q) {
        return conjugate(normalized(q));
    }

    private static PhysicsQuaternion normalized(PhysicsQuaternion q) {
        double n2 = normSquared(q);
        if (!(n2 > 0.0)) {
            throw new IllegalArgumentException("quaternion norm must be > 0");
        }
        double inv = 1.0 / Math.sqrt(n2);
        return new PhysicsQuaternion(q.x() * inv, q.y() * inv, q.z() * inv, q.w() * inv);
    }

    private static double normSquared(PhysicsQuaternion q) {
        return (q.x() * q.x()) + (q.y() * q.y()) + (q.z() * q.z()) + (q.w() * q.w());
    }

    private static PhysicsQuaternion conjugate(PhysicsQuaternion q) {
        return new PhysicsQuaternion(-q.x(), -q.y(), -q.z(), q.w());
    }

    private static PhysicsQuaternion multiply(PhysicsQuaternion a, PhysicsQuaternion b) {
        return new PhysicsQuaternion(
                (a.w() * b.x()) + (a.x() * b.w()) + (a.y() * b.z()) - (a.z() * b.y()),
                (a.w() * b.y()) - (a.x() * b.z()) + (a.y() * b.w()) + (a.z() * b.x()),
                (a.w() * b.z()) + (a.x() * b.y()) - (a.y() * b.x()) + (a.z() * b.w()),
                (a.w() * b.w()) - (a.x() * b.x()) - (a.y() * b.y()) - (a.z() * b.z()));
    }
}
