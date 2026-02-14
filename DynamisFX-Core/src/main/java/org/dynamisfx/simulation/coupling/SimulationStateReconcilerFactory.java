/*
 * Copyright 2024-2026 DynamisFX Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                (objectId, zones) -> DeterministicZoneSelector.select(zones, null, null),
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
