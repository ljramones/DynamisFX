package org.dynamisfx.simulation.orbital;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.dynamisfx.physics.model.ReferenceFrame;

/**
 * Phase-1 orbital engine that serves deterministic scripted trajectories.
 */
public final class ScriptedOrbitalDynamicsEngine implements OrbitalDynamicsEngine {

    private final Map<String, OrbitalTrajectory> trajectoriesByObjectId = new LinkedHashMap<>();

    public void setTrajectory(String objectId, OrbitalTrajectory trajectory) {
        validateObjectId(objectId);
        Objects.requireNonNull(trajectory, "trajectory must not be null");
        trajectoriesByObjectId.put(objectId, trajectory);
    }

    public boolean removeTrajectory(String objectId) {
        validateObjectId(objectId);
        return trajectoriesByObjectId.remove(objectId) != null;
    }

    @Override
    public Map<String, OrbitalState> propagateTo(
            Collection<String> objectIds,
            double simulationTimeSeconds,
            ReferenceFrame outputFrame) {
        if (objectIds == null) {
            throw new IllegalArgumentException("objectIds must not be null");
        }
        if (!Double.isFinite(simulationTimeSeconds)) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite");
        }
        Objects.requireNonNull(outputFrame, "outputFrame must not be null");

        Map<String, OrbitalState> result = new LinkedHashMap<>();
        for (String objectId : objectIds) {
            validateObjectId(objectId);
            OrbitalTrajectory trajectory = trajectoriesByObjectId.get(objectId);
            if (trajectory == null) {
                continue;
            }
            result.put(objectId, trajectory.sample(simulationTimeSeconds, outputFrame));
        }
        return result;
    }

    private static void validateObjectId(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
    }
}
