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

import org.dynamisfx.physics.api.PhysicsBodyHandle;

/**
 * Maps one logical body across general and orbital worlds.
 */
public record HybridBodyLink(
        PhysicsBodyHandle generalBody,
        PhysicsBodyHandle orbitalBody,
        HybridOwnership ownership,
        StateHandoffMode handoffMode,
        ConflictPolicy conflictPolicy,
        double maxPositionDivergenceMeters,
        double maxLinearVelocityDivergenceMetersPerSecond,
        double maxAngularVelocityDivergenceRadiansPerSecond) {

    public HybridBodyLink(
            PhysicsBodyHandle generalBody,
            PhysicsBodyHandle orbitalBody,
            HybridOwnership ownership,
            StateHandoffMode handoffMode) {
        this(
                generalBody,
                orbitalBody,
                ownership,
                handoffMode,
                ConflictPolicy.OVERWRITE,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY);
    }

    public HybridBodyLink(
            PhysicsBodyHandle generalBody,
            PhysicsBodyHandle orbitalBody,
            HybridOwnership ownership,
            StateHandoffMode handoffMode,
            ConflictPolicy conflictPolicy,
            double maxPositionDivergenceMeters) {
        this(
                generalBody,
                orbitalBody,
                ownership,
                handoffMode,
                conflictPolicy,
                maxPositionDivergenceMeters,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY);
    }

    public HybridBodyLink {
        if (generalBody == null || orbitalBody == null || ownership == null
                || handoffMode == null || conflictPolicy == null) {
            throw new IllegalArgumentException(
                    "generalBody, orbitalBody, ownership, handoffMode and conflictPolicy must not be null");
        }
        if (!isValidThreshold(maxPositionDivergenceMeters)) {
            throw new IllegalArgumentException("maxPositionDivergenceMeters must be >= 0 and not NaN");
        }
        if (!isValidThreshold(maxLinearVelocityDivergenceMetersPerSecond)) {
            throw new IllegalArgumentException("maxLinearVelocityDivergenceMetersPerSecond must be >= 0 and not NaN");
        }
        if (!isValidThreshold(maxAngularVelocityDivergenceRadiansPerSecond)) {
            throw new IllegalArgumentException(
                    "maxAngularVelocityDivergenceRadiansPerSecond must be >= 0 and not NaN");
        }
    }

    private static boolean isValidThreshold(double value) {
        return value >= 0.0 && !Double.isNaN(value);
    }
}
