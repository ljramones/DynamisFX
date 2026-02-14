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

package org.dynamisfx.simulation.runtime;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.SimulationClock;
import org.dynamisfx.simulation.SimulationTransformBridge;
import org.dynamisfx.simulation.coupling.CouplingManager;
import org.dynamisfx.simulation.orbital.OrbitalDynamicsEngine;
import org.dynamisfx.simulation.orbital.OrbitalState;

/**
 * Minimal orchestrator coordinating orbital, coupling, rigid, and publish phases in order.
 */
public final class SimulationOrchestrator {

    private final SimulationClock clock;
    private final OrbitalDynamicsEngine orbitalEngine;
    private final CouplingManager couplingManager;
    private final DoubleConsumer rigidStep;
    private final SimulationTransformBridge transformBridge;
    private final Supplier<Map<String, PhysicsBodyState>> rigidStatesSupplier;
    private final Supplier<Collection<String>> objectIdsSupplier;
    private final ReferenceFrame orbitalOutputFrame;
    private final Set<SimulationOrchestratorListener> listeners = new LinkedHashSet<>();

    public SimulationOrchestrator(
            SimulationClock clock,
            OrbitalDynamicsEngine orbitalEngine,
            CouplingManager couplingManager,
            DoubleConsumer rigidStep,
            SimulationTransformBridge transformBridge,
            Supplier<Collection<String>> objectIdsSupplier,
            ReferenceFrame orbitalOutputFrame) {
        this(
                clock,
                orbitalEngine,
                couplingManager,
                rigidStep,
                transformBridge,
                Map::of,
                objectIdsSupplier,
                orbitalOutputFrame);
    }

    public SimulationOrchestrator(
            SimulationClock clock,
            OrbitalDynamicsEngine orbitalEngine,
            CouplingManager couplingManager,
            DoubleConsumer rigidStep,
            SimulationTransformBridge transformBridge,
            Supplier<Map<String, PhysicsBodyState>> rigidStatesSupplier,
            Supplier<Collection<String>> objectIdsSupplier,
            ReferenceFrame orbitalOutputFrame) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.orbitalEngine = Objects.requireNonNull(orbitalEngine, "orbitalEngine must not be null");
        this.couplingManager = Objects.requireNonNull(couplingManager, "couplingManager must not be null");
        this.rigidStep = Objects.requireNonNull(rigidStep, "rigidStep must not be null");
        this.transformBridge = Objects.requireNonNull(transformBridge, "transformBridge must not be null");
        this.rigidStatesSupplier = Objects.requireNonNull(rigidStatesSupplier, "rigidStatesSupplier must not be null");
        this.objectIdsSupplier = Objects.requireNonNull(objectIdsSupplier, "objectIdsSupplier must not be null");
        this.orbitalOutputFrame = Objects.requireNonNull(orbitalOutputFrame, "orbitalOutputFrame must not be null");
    }

    public double tick(double realDeltaSeconds) {
        double simulationTimeSeconds = clock.advance(realDeltaSeconds);

        firePhase(OrchestratorPhase.ORBITAL, simulationTimeSeconds);
        Map<String, OrbitalState> orbitalStates = orbitalEngine.propagateTo(
                objectIdsSupplier.get(),
                simulationTimeSeconds,
                orbitalOutputFrame);
        transformBridge.writeOrbitalStates(orbitalStates);

        firePhase(OrchestratorPhase.COUPLING, simulationTimeSeconds);
        couplingManager.update(simulationTimeSeconds);

        firePhase(OrchestratorPhase.RIGID, simulationTimeSeconds);
        rigidStep.accept(realDeltaSeconds);
        transformBridge.writeRigidStates(rigidStatesSupplier.get());

        firePhase(OrchestratorPhase.PUBLISH, simulationTimeSeconds);
        transformBridge.publish(simulationTimeSeconds);
        return simulationTimeSeconds;
    }

    public void addListener(SimulationOrchestratorListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public boolean removeListener(SimulationOrchestratorListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        synchronized (listeners) {
            return listeners.remove(listener);
        }
    }

    private void firePhase(OrchestratorPhase phase, double simulationTimeSeconds) {
        Set<SimulationOrchestratorListener> snapshot;
        synchronized (listeners) {
            snapshot = new LinkedHashSet<>(listeners);
        }
        for (SimulationOrchestratorListener listener : snapshot) {
            listener.onPhase(phase, simulationTimeSeconds);
        }
    }
}
