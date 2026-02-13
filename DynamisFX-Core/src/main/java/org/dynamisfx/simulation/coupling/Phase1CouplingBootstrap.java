package org.dynamisfx.simulation.coupling;

/**
 * Factory helpers for phase-1 coupling scaffolding.
 */
public final class Phase1CouplingBootstrap {

    public static final double DEFAULT_PROMOTE_DISTANCE_METERS = 1_000.0;
    public static final double DEFAULT_DEMOTE_DISTANCE_METERS = 1_500.0;
    public static final double DEFAULT_COOLDOWN_SECONDS = 1.0;
    public static final double DEFAULT_PREEMPT_INTERCEPT_WINDOW_SECONDS = 2.0;

    private Phase1CouplingBootstrap() {
    }

    public static DefaultCouplingManager createDefaultManager(MutableCouplingObservationProvider observationProvider) {
        return createManager(
                observationProvider,
                DEFAULT_PROMOTE_DISTANCE_METERS,
                DEFAULT_DEMOTE_DISTANCE_METERS,
                DEFAULT_COOLDOWN_SECONDS,
                DEFAULT_PREEMPT_INTERCEPT_WINDOW_SECONDS);
    }

    public static DefaultCouplingManager createManager(
            MutableCouplingObservationProvider observationProvider,
            double promoteDistanceMeters,
            double demoteDistanceMeters,
            double cooldownSeconds) {
        return createManager(
                observationProvider,
                promoteDistanceMeters,
                demoteDistanceMeters,
                cooldownSeconds,
                DEFAULT_PREEMPT_INTERCEPT_WINDOW_SECONDS);
    }

    public static DefaultCouplingManager createManager(
            MutableCouplingObservationProvider observationProvider,
            double promoteDistanceMeters,
            double demoteDistanceMeters,
            double cooldownSeconds,
            double preemptInterceptWindowSeconds) {
        ThresholdTransitionPolicy policy = new ThresholdTransitionPolicy(
                observationProvider,
                promoteDistanceMeters,
                demoteDistanceMeters,
                cooldownSeconds,
                preemptInterceptWindowSeconds);
        return new DefaultCouplingManager(policy, observationProvider);
    }
}
