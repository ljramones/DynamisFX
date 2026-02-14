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

import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;

/**
 * Global orbital state snapshot produced by an orbital dynamics engine.
 */
public record OrbitalState(
        PhysicsVector3 position,
        PhysicsVector3 linearVelocity,
        PhysicsVector3 angularVelocity,
        PhysicsQuaternion orientation,
        ReferenceFrame referenceFrame,
        double timestampSeconds) {

    public OrbitalState(
            PhysicsVector3 position,
            PhysicsVector3 linearVelocity,
            PhysicsQuaternion orientation,
            ReferenceFrame referenceFrame,
            double timestampSeconds) {
        this(position, linearVelocity, PhysicsVector3.ZERO, orientation, referenceFrame, timestampSeconds);
    }

    public OrbitalState {
        if (position == null
                || linearVelocity == null
                || angularVelocity == null
                || orientation == null
                || referenceFrame == null) {
            throw new IllegalArgumentException("state values must not be null");
        }
        if (!Double.isFinite(timestampSeconds)) {
            throw new IllegalArgumentException("timestampSeconds must be finite");
        }
    }
}
