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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.SimulationClock;
import org.dynamisfx.simulation.SimulationTransformBridge;
import org.dynamisfx.simulation.TransformStore;
import org.dynamisfx.simulation.coupling.CouplingDecisionReason;
import org.dynamisfx.simulation.coupling.CouplingTelemetryEvent;
import org.dynamisfx.simulation.coupling.CouplingTransitionDecision;
import org.dynamisfx.simulation.coupling.CouplingTransitionPolicy;
import org.dynamisfx.simulation.coupling.DefaultCouplingManager;
import org.dynamisfx.simulation.entity.SimulationEntityRegistry;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.dynamisfx.simulation.orbital.ScriptedOrbitalDynamicsEngine;
import org.junit.jupiter.api.Test;

class SimulationOrchestratorTest {

    @Test
    void executesTickInExpectedOrderAndEmitsCouplingTelemetry() {
        SimulationEntityRegistry<String> registry = new SimulationEntityRegistry<>();
        registry.register("lander-1", "entity");
        TransformStore store = new TransformStore(1);
        SimulationTransformBridge bridge = new SimulationTransformBridge(registry, store);

        ScriptedOrbitalDynamicsEngine orbital = new ScriptedOrbitalDynamicsEngine();
        orbital.setTrajectory("lander-1", (time, frame) -> new OrbitalState(
                new PhysicsVector3(time, 0, 0),
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                frame,
                time));

        CouplingTransitionPolicy policy = context -> CouplingTransitionDecision.noChange(CouplingDecisionReason.NO_CHANGE);
        DefaultCouplingManager coupling = new DefaultCouplingManager(policy);
        coupling.setMode("lander-1", org.dynamisfx.simulation.ObjectSimulationMode.ORBITAL_ONLY);
        List<CouplingTelemetryEvent> couplingEvents = new ArrayList<>();
        coupling.addTelemetryListener(couplingEvents::add);

        List<String> order = new ArrayList<>();
        SimulationOrchestrator orchestrator = new SimulationOrchestrator(
                new SimulationClock(),
                orbital,
                coupling,
                dt -> order.add("step"),
                bridge,
                () -> Map.of("lander-1", new PhysicsBodyState(
                        new PhysicsVector3(9.0, 0.0, 0.0),
                        PhysicsQuaternion.IDENTITY,
                        PhysicsVector3.ZERO,
                        PhysicsVector3.ZERO,
                        ReferenceFrame.WORLD,
                        0.0)),
                () -> List.of("lander-1"),
                ReferenceFrame.WORLD);
        orchestrator.addListener((phase, t) -> order.add(phase.name().toLowerCase()));

        double simTime = orchestrator.tick(0.5);

        assertEquals(0.5, simTime, 1e-9);
        assertEquals(List.of("orbital", "coupling", "rigid", "step", "publish"), order);
        assertEquals(1, couplingEvents.size());
        assertEquals(CouplingDecisionReason.NO_CHANGE, couplingEvents.get(0).reason());
        assertTrue(couplingEvents.get(0).objectId().equals("lander-1"));
        assertEquals(9.0, store.sample(0).posX(), 1e-9);
    }

    @Test
    void supportsListenerRemovalDuringPhaseCallback() {
        SimulationEntityRegistry<String> registry = new SimulationEntityRegistry<>();
        registry.register("lander-1", "entity");
        SimulationTransformBridge bridge = new SimulationTransformBridge(registry, new TransformStore(1));
        ScriptedOrbitalDynamicsEngine orbital = new ScriptedOrbitalDynamicsEngine();
        orbital.setTrajectory("lander-1", (time, frame) -> new OrbitalState(
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                frame,
                time));
        DefaultCouplingManager coupling = new DefaultCouplingManager(context ->
                CouplingTransitionDecision.noChange(CouplingDecisionReason.NO_CHANGE));
        coupling.setMode("lander-1", org.dynamisfx.simulation.ObjectSimulationMode.ORBITAL_ONLY);
        SimulationOrchestrator orchestrator = new SimulationOrchestrator(
                new SimulationClock(),
                orbital,
                coupling,
                dt -> {
                },
                bridge,
                Map::of,
                () -> List.of("lander-1"),
                ReferenceFrame.WORLD);
        SimulationOrchestratorListener[] ref = new SimulationOrchestratorListener[1];
        ref[0] = (phase, time) -> orchestrator.removeListener(ref[0]);
        orchestrator.addListener(ref[0]);

        double simTime = orchestrator.tick(0.25);
        assertEquals(0.25, simTime, 1e-9);
    }
}
