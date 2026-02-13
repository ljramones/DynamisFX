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
    void validatesConstructorArguments() {
        CouplingObservationProvider provider = new StubObservationProvider(10.0, false);
        assertThrows(NullPointerException.class, () -> new ThresholdTransitionPolicy(null, 1, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTransitionPolicy(provider, 0, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTransitionPolicy(provider, 2, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new ThresholdTransitionPolicy(provider, 1, 2, -1));
    }

    @Test
    void validatesObservationDistance() {
        CouplingObservationProvider provider = new CouplingObservationProvider() {
            @Override
            public OptionalDouble distanceMetersToNearestZone(String objectId, Collection<PhysicsZone> zones) {
                return OptionalDouble.of(Double.NaN);
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

    private static CouplingTransitionContext context(
            ObjectSimulationMode mode,
            double simulationTimeSeconds,
            double lastTransitionTimeSeconds) {
        return new CouplingTransitionContext(
                "lander-1",
                mode,
                simulationTimeSeconds,
                lastTransitionTimeSeconds,
                List.of());
    }

    private record StubObservationProvider(double distanceMeters, boolean contact) implements CouplingObservationProvider {
        @Override
        public OptionalDouble distanceMetersToNearestZone(String objectId, Collection<PhysicsZone> zones) {
            return OptionalDouble.of(distanceMeters);
        }

        @Override
        public boolean hasActiveContact(String objectId) {
            return contact;
        }
    }
}
