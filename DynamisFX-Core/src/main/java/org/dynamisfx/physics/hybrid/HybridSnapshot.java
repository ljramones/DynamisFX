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

package org.dynamisfx.physics.hybrid;

import java.util.Map;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;

/**
 * Immutable snapshot of both worlds at a simulation instant.
 */
public record HybridSnapshot(
        double simulationTimeSeconds,
        double interpolationAlpha,
        double extrapolationSeconds,
        Map<PhysicsBodyHandle, PhysicsBodyState> generalStates,
        Map<PhysicsBodyHandle, PhysicsBodyState> orbitalStates) {

    public HybridSnapshot {
        if (!Double.isFinite(simulationTimeSeconds) || simulationTimeSeconds < 0.0) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite and >= 0");
        }
        if (!Double.isFinite(interpolationAlpha) || interpolationAlpha < 0.0 || interpolationAlpha > 1.0) {
            throw new IllegalArgumentException("interpolationAlpha must be finite and in [0,1]");
        }
        if (!Double.isFinite(extrapolationSeconds) || extrapolationSeconds < 0.0) {
            throw new IllegalArgumentException("extrapolationSeconds must be finite and >= 0");
        }
        if (generalStates == null || orbitalStates == null) {
            throw new IllegalArgumentException("generalStates and orbitalStates must not be null");
        }
        generalStates = Map.copyOf(generalStates);
        orbitalStates = Map.copyOf(orbitalStates);
    }
}
