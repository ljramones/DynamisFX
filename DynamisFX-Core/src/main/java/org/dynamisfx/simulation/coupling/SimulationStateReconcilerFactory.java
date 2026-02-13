package org.dynamisfx.simulation.coupling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.dynamisfx.simulation.SimulationStateBuffers;
import org.dynamisfx.simulation.orbital.OrbitalState;

/**
 * Factory helpers for building coupling state reconcilers from shared state buffers.
 */
public final class SimulationStateReconcilerFactory {

    private SimulationStateReconcilerFactory() {
    }

    public static CouplingStateReconciler create(
            SimulationStateBuffers stateBuffers,
            BiConsumer<String, OrbitalState> orbitalStateSink) {
        return create(
                stateBuffers,
                orbitalStateSink,
                objectId -> {
                },
                (objectId, zones) -> zones.isEmpty() ? Optional.empty() : Optional.of(zones.get(0)),
                snapshot -> {
                });
    }

    public static CouplingStateReconciler create(
            SimulationStateBuffers stateBuffers,
            BiConsumer<String, OrbitalState> orbitalStateSink,
            Consumer<String> orbitalStateClearer,
            BiFunction<String, List<PhysicsZone>, Optional<PhysicsZone>> zoneResolver) {
        return create(stateBuffers, orbitalStateSink, orbitalStateClearer, zoneResolver, snapshot -> {
        });
    }

    public static CouplingStateReconciler create(
            SimulationStateBuffers stateBuffers,
            BiConsumer<String, OrbitalState> orbitalStateSink,
            Consumer<String> orbitalStateClearer,
            BiFunction<String, List<PhysicsZone>, Optional<PhysicsZone>> zoneResolver,
            Consumer<StateHandoffSnapshot> diagnosticsSink) {
        Objects.requireNonNull(stateBuffers, "stateBuffers must not be null");
        Objects.requireNonNull(orbitalStateSink, "orbitalStateSink must not be null");
        Objects.requireNonNull(orbitalStateClearer, "orbitalStateClearer must not be null");
        Objects.requireNonNull(zoneResolver, "zoneResolver must not be null");
        Objects.requireNonNull(diagnosticsSink, "diagnosticsSink must not be null");

        return new CouplingStateReconciler(
                stateBuffers.orbital()::get,
                stateBuffers.rigid()::get,
                stateBuffers.rigid()::put,
                orbitalStateSink,
                stateBuffers.rigid()::remove,
                orbitalStateClearer,
                zoneResolver,
                diagnosticsSink);
    }
}
