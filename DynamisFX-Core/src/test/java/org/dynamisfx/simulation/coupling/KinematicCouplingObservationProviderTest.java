package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class KinematicCouplingObservationProviderTest {

    @Test
    void computesDistanceAndPredictedEnterFromOrbitalState() {
        Map<String, OrbitalState> orbital = Map.of(
                "lander-1",
                new OrbitalState(
                        new PhysicsVector3(150.0, 0.0, 0.0),
                        new PhysicsVector3(-10.0, 0.0, 0.0),
                        PhysicsVector3.ZERO,
                        PhysicsQuaternion.IDENTITY,
                        ReferenceFrame.WORLD,
                        1.0));
        KinematicCouplingObservationProvider provider = new KinematicCouplingObservationProvider(
                objectId -> Optional.empty(),
                objectId -> Optional.ofNullable(orbital.get(objectId)));

        OptionalDouble distance = provider.distanceMetersToNearestZone("lander-1", List.of(new StubZone(100.0)));
        OptionalDouble intercept = provider.predictedInterceptSeconds("lander-1", List.of(new StubZone(100.0)));

        assertEquals(50.0, distance.orElseThrow(), 1e-9);
        assertEquals(5.0, intercept.orElseThrow(), 1e-9);
    }

    @Test
    void computesPredictedExitFromRigidState() {
        Map<String, PhysicsBodyState> rigid = Map.of(
                "lander-1",
                new PhysicsBodyState(
                        new PhysicsVector3(90.0, 0.0, 0.0),
                        PhysicsQuaternion.IDENTITY,
                        new PhysicsVector3(2.0, 0.0, 0.0),
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        1.0));
        KinematicCouplingObservationProvider provider = new KinematicCouplingObservationProvider(
                objectId -> Optional.ofNullable(rigid.get(objectId)),
                objectId -> Optional.empty());

        OptionalDouble intercept = provider.predictedInterceptSeconds("lander-1", List.of(new StubZone(100.0)));
        assertEquals(5.0, intercept.orElseThrow(), 1e-9);
    }

    @Test
    void delegatesContactAndAltitudeToFallback() {
        CouplingObservationProvider fallback = new CouplingObservationProvider() {
            @Override
            public OptionalDouble distanceMetersToNearestZone(String objectId, java.util.Collection<PhysicsZone> zones) {
                return OptionalDouble.empty();
            }

            @Override
            public OptionalDouble predictedInterceptSeconds(String objectId, java.util.Collection<PhysicsZone> zones) {
                return OptionalDouble.empty();
            }

            @Override
            public boolean hasActiveContact(String objectId) {
                return true;
            }

            @Override
            public OptionalDouble altitudeMetersAboveSurface(String objectId, java.util.Collection<PhysicsZone> zones) {
                return OptionalDouble.of(12.0);
            }
        };
        KinematicCouplingObservationProvider provider = new KinematicCouplingObservationProvider(
                objectId -> Optional.empty(),
                objectId -> Optional.empty(),
                fallback);

        assertTrue(provider.hasActiveContact("lander-1"));
        assertEquals(12.0, provider.altitudeMetersAboveSurface("lander-1", List.of(new StubZone(100.0))).orElseThrow(), 1e-9);
    }

    @Test
    void returnsEmptyPredictedInterceptWhenNoBoundaryCrossing() {
        Map<String, OrbitalState> orbital = Map.of(
                "lander-1",
                new OrbitalState(
                        new PhysicsVector3(150.0, 0.0, 0.0),
                        new PhysicsVector3(10.0, 0.0, 0.0),
                        PhysicsVector3.ZERO,
                        PhysicsQuaternion.IDENTITY,
                        ReferenceFrame.WORLD,
                        1.0));
        KinematicCouplingObservationProvider provider = new KinematicCouplingObservationProvider(
                objectId -> Optional.empty(),
                objectId -> Optional.ofNullable(orbital.get(objectId)));

        assertFalse(provider.predictedInterceptSeconds("lander-1", List.of(new StubZone(100.0))).isPresent());
    }

    private record StubZone(double radiusMeters) implements PhysicsZone {
        @Override
        public ZoneId zoneId() {
            return new ZoneId("zone-a");
        }

        @Override
        public ReferenceFrame anchorFrame() {
            return ReferenceFrame.WORLD;
        }

        @Override
        public PhysicsVector3 anchorPosition() {
            return PhysicsVector3.ZERO;
        }

        @Override
        public RigidBodyWorld world() {
            return null;
        }
    }
}
