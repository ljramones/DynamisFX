package org.dynamisfx.simulation.coupling;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.orbital.OrbitalState;

/**
 * Observation provider that derives distance and predicted boundary crossing from kinematic state.
 */
public final class KinematicCouplingObservationProvider implements CouplingObservationProvider {

    private final Function<String, Optional<PhysicsBodyState>> rigidStateSource;
    private final Function<String, Optional<OrbitalState>> orbitalStateSource;
    private final CouplingObservationProvider fallbackProvider;

    public KinematicCouplingObservationProvider(
            Function<String, Optional<PhysicsBodyState>> rigidStateSource,
            Function<String, Optional<OrbitalState>> orbitalStateSource) {
        this(rigidStateSource, orbitalStateSource, new EmptyObservationProvider());
    }

    public KinematicCouplingObservationProvider(
            Function<String, Optional<PhysicsBodyState>> rigidStateSource,
            Function<String, Optional<OrbitalState>> orbitalStateSource,
            CouplingObservationProvider fallbackProvider) {
        this.rigidStateSource = Objects.requireNonNull(rigidStateSource, "rigidStateSource must not be null");
        this.orbitalStateSource = Objects.requireNonNull(orbitalStateSource, "orbitalStateSource must not be null");
        this.fallbackProvider = Objects.requireNonNull(fallbackProvider, "fallbackProvider must not be null");
    }

    @Override
    public OptionalDouble distanceMetersToNearestZone(String objectId, Collection<PhysicsZone> zones) {
        KinematicState state = resolveState(objectId);
        if (state == null || zones == null || zones.isEmpty()) {
            return OptionalDouble.empty();
        }
        double best = Double.POSITIVE_INFINITY;
        boolean found = false;
        for (PhysicsZone zone : zones) {
            if (zone == null || zone.anchorFrame() != state.referenceFrame()) {
                continue;
            }
            PhysicsVector3 relative = subtract(state.position(), zone.anchorPosition());
            double radialDistance = norm(relative);
            double boundaryDistance = Math.abs(radialDistance - zone.radiusMeters());
            if (boundaryDistance < best) {
                best = boundaryDistance;
            }
            found = true;
        }
        return found ? OptionalDouble.of(best) : OptionalDouble.empty();
    }

    @Override
    public OptionalDouble predictedInterceptSeconds(String objectId, Collection<PhysicsZone> zones) {
        KinematicState state = resolveState(objectId);
        if (state == null || zones == null || zones.isEmpty()) {
            return OptionalDouble.empty();
        }
        double best = Double.POSITIVE_INFINITY;
        boolean found = false;
        for (PhysicsZone zone : zones) {
            if (zone == null || zone.anchorFrame() != state.referenceFrame()) {
                continue;
            }
            OptionalDouble crossing = timeToSphereBoundary(
                    subtract(state.position(), zone.anchorPosition()),
                    state.linearVelocity(),
                    zone.radiusMeters());
            if (crossing.isPresent()) {
                best = Math.min(best, crossing.orElseThrow());
                found = true;
            }
        }
        return found ? OptionalDouble.of(best) : OptionalDouble.empty();
    }

    @Override
    public boolean hasActiveContact(String objectId) {
        return fallbackProvider.hasActiveContact(objectId);
    }

    @Override
    public OptionalDouble altitudeMetersAboveSurface(String objectId, Collection<PhysicsZone> zones) {
        return fallbackProvider.altitudeMetersAboveSurface(objectId, zones);
    }

    private KinematicState resolveState(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        Optional<PhysicsBodyState> rigidState = rigidStateSource.apply(objectId);
        if (rigidState.isPresent()) {
            PhysicsBodyState state = rigidState.get();
            return new KinematicState(state.position(), state.linearVelocity(), state.referenceFrame());
        }
        Optional<OrbitalState> orbitalState = orbitalStateSource.apply(objectId);
        if (orbitalState.isPresent()) {
            OrbitalState state = orbitalState.get();
            return new KinematicState(state.position(), state.linearVelocity(), state.referenceFrame());
        }
        return null;
    }

    private static OptionalDouble timeToSphereBoundary(PhysicsVector3 relativePosition, PhysicsVector3 velocity, double radiusMeters) {
        if (!Double.isFinite(radiusMeters) || radiusMeters <= 0.0) {
            return OptionalDouble.empty();
        }
        double vx = velocity.x();
        double vy = velocity.y();
        double vz = velocity.z();
        double a = (vx * vx) + (vy * vy) + (vz * vz);
        if (a <= 0.0) {
            return OptionalDouble.empty();
        }
        double px = relativePosition.x();
        double py = relativePosition.y();
        double pz = relativePosition.z();
        double b = 2.0 * ((px * vx) + (py * vy) + (pz * vz));
        double c = (px * px) + (py * py) + (pz * pz) - (radiusMeters * radiusMeters);
        double disc = (b * b) - (4.0 * a * c);
        if (disc < 0.0) {
            return OptionalDouble.empty();
        }
        double sqrtDisc = Math.sqrt(disc);
        double inv = 1.0 / (2.0 * a);
        double t1 = (-b - sqrtDisc) * inv;
        double t2 = (-b + sqrtDisc) * inv;
        double best = Double.POSITIVE_INFINITY;
        if (t1 >= 0.0) {
            best = Math.min(best, t1);
        }
        if (t2 >= 0.0) {
            best = Math.min(best, t2);
        }
        return Double.isFinite(best) ? OptionalDouble.of(best) : OptionalDouble.empty();
    }

    private static PhysicsVector3 subtract(PhysicsVector3 a, PhysicsVector3 b) {
        return new PhysicsVector3(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
    }

    private static double norm(PhysicsVector3 vector) {
        return Math.sqrt(
                (vector.x() * vector.x())
                        + (vector.y() * vector.y())
                        + (vector.z() * vector.z()));
    }

    private record KinematicState(PhysicsVector3 position, PhysicsVector3 linearVelocity, ReferenceFrame referenceFrame) {
        private KinematicState {
            Objects.requireNonNull(position, "position must not be null");
            Objects.requireNonNull(linearVelocity, "linearVelocity must not be null");
            Objects.requireNonNull(referenceFrame, "referenceFrame must not be null");
        }
    }

    private static final class EmptyObservationProvider implements CouplingObservationProvider {
        @Override
        public OptionalDouble distanceMetersToNearestZone(String objectId, Collection<PhysicsZone> zones) {
            return OptionalDouble.empty();
        }

        @Override
        public OptionalDouble predictedInterceptSeconds(String objectId, Collection<PhysicsZone> zones) {
            return OptionalDouble.empty();
        }

        @Override
        public boolean hasActiveContact(String objectId) {
            return false;
        }
    }
}
