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

package org.dynamisfx.simulation;

import java.util.Map;
import java.util.OptionalInt;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.simulation.entity.SimulationEntityRegistry;
import org.dynamisfx.simulation.orbital.OrbitalState;

/**
 * Bridges simulation state snapshots into the shared transform store.
 */
public final class SimulationTransformBridge {

    private final SimulationEntityRegistry<?> registry;
    private final TransformStore transformStore;

    public SimulationTransformBridge(
            SimulationEntityRegistry<?> registry,
            TransformStore transformStore) {
        if (registry == null || transformStore == null) {
            throw new IllegalArgumentException("registry and transformStore must not be null");
        }
        this.registry = registry;
        this.transformStore = transformStore;
    }

    public void writeOrbitalStates(Map<String, OrbitalState> states) {
        if (states == null) {
            throw new IllegalArgumentException("states must not be null");
        }
        states.forEach((objectId, state) -> {
            OptionalInt index = registry.indexOf(objectId);
            if (index.isEmpty()) {
                return;
            }
            transformStore.setTransform(
                    index.getAsInt(),
                    state.position().x(),
                    state.position().y(),
                    state.position().z(),
                    state.orientation().x(),
                    state.orientation().y(),
                    state.orientation().z(),
                    state.orientation().w());
        });
    }

    public void writeRigidStates(Map<String, PhysicsBodyState> states) {
        if (states == null) {
            throw new IllegalArgumentException("states must not be null");
        }
        states.forEach((objectId, state) -> {
            OptionalInt index = registry.indexOf(objectId);
            if (index.isEmpty()) {
                return;
            }
            transformStore.setTransform(
                    index.getAsInt(),
                    state.position().x(),
                    state.position().y(),
                    state.position().z(),
                    state.orientation().x(),
                    state.orientation().y(),
                    state.orientation().z(),
                    state.orientation().w());
        });
    }

    public void publish(double simulationTimeSeconds) {
        transformStore.publish(simulationTimeSeconds);
    }
}
