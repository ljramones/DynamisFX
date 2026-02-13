package org.dynamisfx.physics.orekit;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.orbital.OrbitalDynamicsEngine;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.orekit.time.AbsoluteDate;

/**
 * Adapter seam exposing an {@link OrekitWorld} as a simulation-level orbital dynamics engine.
 */
public final class OrekitOrbitalDynamicsAdapter implements OrbitalDynamicsEngine {

    private final OrekitWorld world;
    private final Map<String, PhysicsBodyHandle> handlesByObjectId = new LinkedHashMap<>();
    private double simulationTimeSeconds;

    public OrekitOrbitalDynamicsAdapter(OrekitWorld world) {
        this.world = Objects.requireNonNull(world, "world must not be null");
    }

    public void registerBody(String objectId, PhysicsBodyHandle handle) {
        validateObjectId(objectId);
        Objects.requireNonNull(handle, "handle must not be null");
        handlesByObjectId.put(objectId, handle);
    }

    public boolean unregisterBody(String objectId) {
        validateObjectId(objectId);
        return handlesByObjectId.remove(objectId) != null;
    }

    @Override
    public synchronized Map<String, OrbitalState> propagateTo(
            Collection<String> objectIds,
            double targetSimulationTimeSeconds,
            ReferenceFrame outputFrame) {
        Objects.requireNonNull(objectIds, "objectIds must not be null");
        Objects.requireNonNull(outputFrame, "outputFrame must not be null");
        if (!Double.isFinite(targetSimulationTimeSeconds)) {
            throw new IllegalArgumentException("targetSimulationTimeSeconds must be finite");
        }
        if (targetSimulationTimeSeconds < simulationTimeSeconds) {
            throw new IllegalArgumentException("targetSimulationTimeSeconds must be monotonic");
        }
        double dt = targetSimulationTimeSeconds - simulationTimeSeconds;
        if (dt > 0.0) {
            world.step(dt);
            simulationTimeSeconds = targetSimulationTimeSeconds;
        }

        Map<String, OrbitalState> result = new LinkedHashMap<>();
        AbsoluteDate date = AbsoluteDate.J2000_EPOCH.shiftedBy(simulationTimeSeconds);
        for (String objectId : objectIds) {
            validateObjectId(objectId);
            PhysicsBodyHandle handle = handlesByObjectId.get(objectId);
            if (handle == null) {
                continue;
            }
            PhysicsBodyState state = world.getBodyState(handle);
            PhysicsBodyState converted = OrekitFrameBridge.transformState(state, outputFrame, date);
            result.put(objectId, new OrbitalState(
                    converted.position(),
                    converted.linearVelocity(),
                    converted.orientation(),
                    converted.referenceFrame(),
                    simulationTimeSeconds));
        }
        return result;
    }

    @Override
    public synchronized void close() {
        world.close();
    }

    private static void validateObjectId(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
    }
}
