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
import java.util.function.Function;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.orbital.OrbitalState;

/**
 * Reconciles object state when authority moves between orbital and local rigid simulation.
 */
public final class CouplingStateReconciler implements CouplingTransitionListener {

    private final Function<String, Optional<OrbitalState>> orbitalStateSource;
    private final Function<String, Optional<PhysicsBodyState>> rigidStateSource;
    private final BiConsumer<String, PhysicsBodyState> rigidStateSink;
    private final BiConsumer<String, OrbitalState> orbitalStateSink;
    private final Consumer<String> rigidStateClearer;
    private final Consumer<String> orbitalStateClearer;
    private final BiFunction<String, List<PhysicsZone>, Optional<PhysicsZone>> zoneResolver;
    private final Consumer<StateHandoffSnapshot> diagnosticsSink;

    public CouplingStateReconciler(
            Function<String, Optional<OrbitalState>> orbitalStateSource,
            Function<String, Optional<PhysicsBodyState>> rigidStateSource,
            BiConsumer<String, PhysicsBodyState> rigidStateSink,
            BiConsumer<String, OrbitalState> orbitalStateSink) {
        this(
                orbitalStateSource,
                rigidStateSource,
                rigidStateSink,
                orbitalStateSink,
                objectId -> {
                },
                objectId -> {
                },
                (objectId, zones) -> DeterministicZoneSelector.select(zones, null, null),
                snapshot -> {
                });
    }

    public CouplingStateReconciler(
            Function<String, Optional<OrbitalState>> orbitalStateSource,
            Function<String, Optional<PhysicsBodyState>> rigidStateSource,
            BiConsumer<String, PhysicsBodyState> rigidStateSink,
            BiConsumer<String, OrbitalState> orbitalStateSink,
            Consumer<String> rigidStateClearer,
            Consumer<String> orbitalStateClearer,
            BiFunction<String, List<PhysicsZone>, Optional<PhysicsZone>> zoneResolver) {
        this(
                orbitalStateSource,
                rigidStateSource,
                rigidStateSink,
                orbitalStateSink,
                rigidStateClearer,
                orbitalStateClearer,
                zoneResolver,
                snapshot -> {
                });
    }

    public CouplingStateReconciler(
            Function<String, Optional<OrbitalState>> orbitalStateSource,
            Function<String, Optional<PhysicsBodyState>> rigidStateSource,
            BiConsumer<String, PhysicsBodyState> rigidStateSink,
            BiConsumer<String, OrbitalState> orbitalStateSink,
            Consumer<String> rigidStateClearer,
            Consumer<String> orbitalStateClearer,
            BiFunction<String, List<PhysicsZone>, Optional<PhysicsZone>> zoneResolver,
            Consumer<StateHandoffSnapshot> diagnosticsSink) {
        this.orbitalStateSource = Objects.requireNonNull(orbitalStateSource, "orbitalStateSource must not be null");
        this.rigidStateSource = Objects.requireNonNull(rigidStateSource, "rigidStateSource must not be null");
        this.rigidStateSink = Objects.requireNonNull(rigidStateSink, "rigidStateSink must not be null");
        this.orbitalStateSink = Objects.requireNonNull(orbitalStateSink, "orbitalStateSink must not be null");
        this.rigidStateClearer = Objects.requireNonNull(rigidStateClearer, "rigidStateClearer must not be null");
        this.orbitalStateClearer = Objects.requireNonNull(orbitalStateClearer, "orbitalStateClearer must not be null");
        this.zoneResolver = Objects.requireNonNull(zoneResolver, "zoneResolver must not be null");
        this.diagnosticsSink = Objects.requireNonNull(diagnosticsSink, "diagnosticsSink must not be null");
    }

    @Override
    public void onTransition(CouplingModeTransitionEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        String objectId = event.objectId();
        Optional<PhysicsZone> zoneOptional = zoneResolver.apply(objectId, event.zones());
        if (zoneOptional.isEmpty()) {
            return;
        }
        PhysicsZone zone = zoneOptional.get();
        ObjectSimulationMode fromMode = event.fromMode();
        ObjectSimulationMode toMode = event.toMode();
        if (fromMode != ObjectSimulationMode.PHYSICS_ACTIVE && toMode == ObjectSimulationMode.PHYSICS_ACTIVE) {
            promoteToPhysics(objectId, event.simulationTimeSeconds(), zone);
            return;
        }
        if (fromMode == ObjectSimulationMode.PHYSICS_ACTIVE && toMode != ObjectSimulationMode.PHYSICS_ACTIVE) {
            demoteToOrbital(objectId, event.simulationTimeSeconds(), zone);
        }
    }

    private void promoteToPhysics(String objectId, double simulationTimeSeconds, PhysicsZone zone) {
        Optional<OrbitalState> orbitalStateOptional = orbitalStateSource.apply(objectId);
        if (orbitalStateOptional.isEmpty()) {
            return;
        }
        OrbitalState orbitalState = orbitalStateOptional.get();
        PhysicsBodyState seeded = ZoneFrameTransform.orbitalToLocalRigid(orbitalState, simulationTimeSeconds, zone);
        diagnosticsSink.accept(new StateHandoffSnapshot(
                StateHandoffDirection.PROMOTE_TO_PHYSICS,
                simulationTimeSeconds,
                objectId,
                zone.zoneId(),
                zone.anchorPosition(),
                orbitalState.position(),
                orbitalState.linearVelocity(),
                orbitalState.angularVelocity(),
                orbitalState.orientation(),
                seeded.position(),
                seeded.linearVelocity(),
                seeded.angularVelocity(),
                seeded.orientation()));
        rigidStateSink.accept(objectId, seeded);
        orbitalStateClearer.accept(objectId);
    }

    private void demoteToOrbital(String objectId, double simulationTimeSeconds, PhysicsZone zone) {
        Optional<PhysicsBodyState> rigidStateOptional = rigidStateSource.apply(objectId);
        if (rigidStateOptional.isEmpty()) {
            return;
        }
        PhysicsBodyState rigidState = rigidStateOptional.get();
        OrbitalState seeded = ZoneFrameTransform.localRigidToOrbital(rigidState, simulationTimeSeconds, zone);
        diagnosticsSink.accept(new StateHandoffSnapshot(
                StateHandoffDirection.DEMOTE_TO_ORBITAL,
                simulationTimeSeconds,
                objectId,
                zone.zoneId(),
                zone.anchorPosition(),
                seeded.position(),
                seeded.linearVelocity(),
                seeded.angularVelocity(),
                seeded.orientation(),
                rigidState.position(),
                rigidState.linearVelocity(),
                rigidState.angularVelocity(),
                rigidState.orientation()));
        orbitalStateSink.accept(objectId, seeded);
        rigidStateClearer.accept(objectId);
    }
}
