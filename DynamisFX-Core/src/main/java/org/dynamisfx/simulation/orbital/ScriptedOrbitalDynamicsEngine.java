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
