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

package org.dynamisfx.physics.model;

/**
 * Immutable body state snapshot.
 */
public record PhysicsBodyState(
        PhysicsVector3 position,
        PhysicsQuaternion orientation,
        PhysicsVector3 linearVelocity,
        PhysicsVector3 angularVelocity,
        ReferenceFrame referenceFrame,
        double timestampSeconds) {

    public static final PhysicsBodyState IDENTITY = new PhysicsBodyState(
            PhysicsVector3.ZERO,
            PhysicsQuaternion.IDENTITY,
            PhysicsVector3.ZERO,
            PhysicsVector3.ZERO,
            ReferenceFrame.UNSPECIFIED,
            0.0);

    public PhysicsBodyState {
        if (position == null || orientation == null
                || linearVelocity == null || angularVelocity == null
                || referenceFrame == null) {
            throw new IllegalArgumentException("state values must not be null");
        }
        if (!Double.isFinite(timestampSeconds)) {
            throw new IllegalArgumentException("timestampSeconds must be finite");
        }
    }
}
