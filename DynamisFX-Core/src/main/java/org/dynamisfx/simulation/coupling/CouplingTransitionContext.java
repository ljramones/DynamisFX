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

import java.util.Collection;
import java.util.OptionalDouble;
import java.util.Objects;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Immutable context passed to transition policy evaluation.
 */
public record CouplingTransitionContext(
        String objectId,
        ObjectSimulationMode currentMode,
        double simulationTimeSeconds,
        double lastTransitionTimeSeconds,
        OptionalDouble predictedInterceptSeconds,
        Collection<PhysicsZone> zones) {

    public CouplingTransitionContext {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        Objects.requireNonNull(currentMode, "currentMode must not be null");
        Objects.requireNonNull(predictedInterceptSeconds, "predictedInterceptSeconds must not be null");
        Objects.requireNonNull(zones, "zones must not be null");
        if (!Double.isFinite(simulationTimeSeconds)) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite");
        }
        if (!Double.isFinite(lastTransitionTimeSeconds)) {
            throw new IllegalArgumentException("lastTransitionTimeSeconds must be finite");
        }
    }
}
