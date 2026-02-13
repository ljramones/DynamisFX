package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.junit.jupiter.api.Test;

class ThresholdTransitionPolicyTest {

    @Test
    void promotesWhenWithinPromoteDistance() {
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(
                new StubObservationProvider(50.0, false),
                100.0,
                150.0,
                1.0);

        CouplingTransitionDecision next = policy.evaluate(context(ObjectSimulationMode.ORBITAL_ONLY, 10.0, -1.0));

        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, next.nextMode().orElseThrow());
        assertEquals(CouplingDecisionReason.PROMOTE_DISTANCE_THRESHOLD, next.reason());
    }

    @Test
    void demotesWhenOutsideDemoteDistanceAndNoContact() {
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(
                new StubObservationProvider(200.0, false),
                100.0,
                150.0,
                1.0);

        CouplingTransitionDecision next = policy.evaluate(context(ObjectSimulationMode.PHYSICS_ACTIVE, 10.0, -1.0));

        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, next.nextMode().orElseThrow());
        assertEquals(CouplingDecisionReason.DEMOTE_DISTANCE_THRESHOLD, next.reason());
    }

    @Test
    void doesNotDemoteWithActiveContact() {
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(
                new StubObservationProvider(200.0, true),
                100.0,
                150.0,
                1.0);

        CouplingTransitionDecision next = policy.evaluate(context(ObjectSimulationMode.PHYSICS_ACTIVE, 10.0, -1.0));

        assertTrue(next.nextMode().isEmpty());
        assertEquals(CouplingDecisionReason.BLOCKED_BY_CONTACT, next.reason());
    }

    @Test
    void honorsCooldownWindow() {
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(
                new StubObservationProvider(50.0, false),
                100.0,
                150.0,
                5.0);

        CouplingTransitionDecision duringCooldown = policy.evaluate(
                context(ObjectSimulationMode.ORBITAL_ONLY, 12.0, 10.0));
        CouplingTransitionDecision afterCooldown = policy.evaluate(
                context(ObjectSimulationMode.ORBITAL_ONLY, 16.0, 10.0));

        assertTrue(duringCooldown.nextMode().isEmpty());
        assertEquals(CouplingDecisionReason.BLOCKED_BY_COOLDOWN, duringCooldown.reason());
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, afterCooldown.nextMode().orElseThrow());
    }

    @Test
    void promotesOnPredictedInterceptWindow() {
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(
                new StubObservationProvider(500.0, false),
                100.0,
                150.0,
                0.0,
                2.0);

        CouplingTransitionDecision next = policy.evaluate(context(
                ObjectSimulationMode.ORBITAL_ONLY,
                10.0,
                -1.0,
                1.5));

        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, next.nextMode().orElseThrow());
        assertEquals(CouplingDecisionReason.PROMOTE_PREDICTED_INTERCEPT, next.reason());
    }

    @Test
    void validatesConstructorArguments() {
        CouplingObservationProvider provider = new StubObservationProvider(10.0, false);
        assertThrows(NullPointerException.class, () -> new ThresholdTransitionPolicy(null, 1, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTransitionPolicy(provider, 0, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTransitionPolicy(provider, 2, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTransitionPolicy(provider, 1, 2, -1));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTransitionPolicy(provider, 1, 2, 0, 0, -1, 5));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTransitionPolicy(provider, 1, 2, 0, 0, 10, 5));
    }

    @Test
    void validatesObservationDistance() {
        CouplingObservationProvider provider = new CouplingObservationProvider() {
            @Override
            public OptionalDouble distanceMetersToNearestZone(String objectId, Collection<PhysicsZone> zones) {
                return OptionalDouble.of(Double.NaN);
            }

            @Override
            public OptionalDouble predictedInterceptSeconds(String objectId, Collection<PhysicsZone> zones) {
                return OptionalDouble.empty();
            }

            @Override
            public boolean hasActiveContact(String objectId) {
                return false;
            }
        };
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(
                provider,
                100.0,
                150.0,
                0.0);

        assertThrows(IllegalArgumentException.class, () ->
                policy.evaluate(context(ObjectSimulationMode.ORBITAL_ONLY, 1.0, -1.0)));
    }

    @Test
    void promotesOnAltitudeThresholdWhenConfigured() {
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(
                new StubObservationProvider(500.0, false, 40.0),
                100.0,
                150.0,
                0.0,
                0.0,
                50.0,
                80.0);

        CouplingTransitionDecision next = policy.evaluate(context(ObjectSimulationMode.ORBITAL_ONLY, 1.0, -1.0));

        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, next.nextMode().orElseThrow());
        assertEquals(CouplingDecisionReason.PROMOTE_ALTITUDE_THRESHOLD, next.reason());
    }

    @Test
    void demotesOnAltitudeThresholdWhenConfiguredAndNoContact() {
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(
                new StubObservationProvider(10.0, false, 120.0),
                100.0,
                150.0,
                0.0,
                0.0,
                50.0,
                80.0);

        CouplingTransitionDecision next = policy.evaluate(context(ObjectSimulationMode.PHYSICS_ACTIVE, 2.0, -1.0));

        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, next.nextMode().orElseThrow());
        assertEquals(CouplingDecisionReason.DEMOTE_ALTITUDE_THRESHOLD, next.reason());
    }

    @Test
    void remainsStableBetweenPromoteAndDemoteAltitudeThresholds() {
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(
                new StubObservationProvider(10.0, false, 350.0),
                100.0,
                150.0,
                0.0,
                0.0,
                400.0,
                900.0);

        CouplingTransitionDecision promote = policy.evaluate(context(ObjectSimulationMode.ORBITAL_ONLY, 1.0, -1.0));
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, promote.nextMode().orElseThrow());

        ThresholdTransitionPolicy midBandPolicy = new ThresholdTransitionPolicy(
                new StubObservationProvider(10.0, false, 600.0),
                100.0,
                150.0,
                0.0,
                0.0,
                400.0,
                900.0);
        CouplingTransitionDecision midBand = midBandPolicy.evaluate(context(ObjectSimulationMode.PHYSICS_ACTIVE, 2.0, 1.0));
        assertTrue(midBand.nextMode().isEmpty());
        assertEquals(CouplingDecisionReason.NO_CHANGE, midBand.reason());
    }

    private static CouplingTransitionContext context(
            ObjectSimulationMode mode,
            double simulationTimeSeconds,
            double lastTransitionTimeSeconds) {
        return context(mode, simulationTimeSeconds, lastTransitionTimeSeconds, null);
    }

    private static CouplingTransitionContext context(
            ObjectSimulationMode mode,
            double simulationTimeSeconds,
            double lastTransitionTimeSeconds,
            Double predictedInterceptSeconds) {
        return new CouplingTransitionContext(
                "lander-1",
                mode,
                simulationTimeSeconds,
                lastTransitionTimeSeconds,
                predictedInterceptSeconds == null
                        ? OptionalDouble.empty()
                        : OptionalDouble.of(predictedInterceptSeconds),
                List.of());
    }

    private record StubObservationProvider(double distanceMeters, boolean contact, double altitudeMeters)
            implements CouplingObservationProvider {
        private StubObservationProvider(double distanceMeters, boolean contact) {
            this(distanceMeters, contact, Double.NaN);
        }

        @Override
        public OptionalDouble distanceMetersToNearestZone(String objectId, Collection<PhysicsZone> zones) {
            return OptionalDouble.of(distanceMeters);
        }

        @Override
        public OptionalDouble predictedInterceptSeconds(String objectId, Collection<PhysicsZone> zones) {
            return OptionalDouble.empty();
        }

        @Override
        public boolean hasActiveContact(String objectId) {
            return contact;
        }

        @Override
        public OptionalDouble altitudeMetersAboveSurface(String objectId, Collection<PhysicsZone> zones) {
            return Double.isFinite(altitudeMeters) ? OptionalDouble.of(altitudeMeters) : OptionalDouble.empty();
        }
    }
}
