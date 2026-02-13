package org.dynamisfx.simulation.coupling;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Objects;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Distance-threshold transition policy with cooldown and hysteresis support.
 */
public final class ThresholdTransitionPolicy implements CouplingTransitionPolicy {

    private final CouplingObservationProvider observationProvider;
    private final double promoteDistanceMeters;
    private final double demoteDistanceMeters;
    private final double cooldownSeconds;

    public ThresholdTransitionPolicy(
            CouplingObservationProvider observationProvider,
            double promoteDistanceMeters,
            double demoteDistanceMeters,
            double cooldownSeconds) {
        this.observationProvider = Objects.requireNonNull(observationProvider, "observationProvider must not be null");
        if (!Double.isFinite(promoteDistanceMeters) || promoteDistanceMeters <= 0.0) {
            throw new IllegalArgumentException("promoteDistanceMeters must be finite and > 0");
        }
        if (!Double.isFinite(demoteDistanceMeters) || demoteDistanceMeters <= 0.0) {
            throw new IllegalArgumentException("demoteDistanceMeters must be finite and > 0");
        }
        if (demoteDistanceMeters < promoteDistanceMeters) {
            throw new IllegalArgumentException("demoteDistanceMeters must be >= promoteDistanceMeters");
        }
        if (!Double.isFinite(cooldownSeconds) || cooldownSeconds < 0.0) {
            throw new IllegalArgumentException("cooldownSeconds must be finite and >= 0");
        }
        this.promoteDistanceMeters = promoteDistanceMeters;
        this.demoteDistanceMeters = demoteDistanceMeters;
        this.cooldownSeconds = cooldownSeconds;
    }

    @Override
    public Optional<ObjectSimulationMode> evaluate(CouplingTransitionContext context) {
        Objects.requireNonNull(context, "context must not be null");
        if (isInCooldown(context)) {
            return Optional.empty();
        }

        OptionalDouble distanceMeters = observationProvider.distanceMetersToNearestZone(context.objectId(), context.zones());
        if (distanceMeters.isEmpty()) {
            return Optional.empty();
        }
        double distance = distanceMeters.orElseThrow();
        if (!Double.isFinite(distance) || distance < 0.0) {
            throw new IllegalArgumentException("distance observation must be finite and >= 0");
        }

        ObjectSimulationMode mode = context.currentMode();
        if (mode == ObjectSimulationMode.ORBITAL_ONLY) {
            if (distance <= promoteDistanceMeters) {
                return Optional.of(ObjectSimulationMode.PHYSICS_ACTIVE);
            }
            return Optional.empty();
        }

        if (mode == ObjectSimulationMode.PHYSICS_ACTIVE) {
            if (distance >= demoteDistanceMeters && !observationProvider.hasActiveContact(context.objectId())) {
                return Optional.of(ObjectSimulationMode.ORBITAL_ONLY);
            }
            return Optional.empty();
        }

        return Optional.empty();
    }

    private boolean isInCooldown(CouplingTransitionContext context) {
        if (context.lastTransitionTimeSeconds() < 0.0) {
            return false;
        }
        return context.simulationTimeSeconds() < (context.lastTransitionTimeSeconds() + cooldownSeconds);
    }
}
