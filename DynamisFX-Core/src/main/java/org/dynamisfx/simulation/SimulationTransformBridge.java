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
