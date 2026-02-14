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

import java.util.Objects;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Captures seed values used when handing off state between global orbital and local rigid simulation.
 */
public record StateHandoffSnapshot(
        StateHandoffDirection direction,
        double simulationTimeSeconds,
        String objectId,
        ZoneId zoneId,
        PhysicsVector3 zoneAnchorPosition,
        PhysicsVector3 globalPosition,
        PhysicsVector3 globalVelocity,
        PhysicsVector3 globalAngularVelocity,
        PhysicsQuaternion globalOrientation,
        PhysicsVector3 localPosition,
        PhysicsVector3 localVelocity,
        PhysicsVector3 localAngularVelocity,
        PhysicsQuaternion localOrientation) {

    public StateHandoffSnapshot {
        Objects.requireNonNull(direction, "direction must not be null");
        if (!Double.isFinite(simulationTimeSeconds)) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite");
        }
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        Objects.requireNonNull(zoneAnchorPosition, "zoneAnchorPosition must not be null");
        Objects.requireNonNull(globalPosition, "globalPosition must not be null");
        Objects.requireNonNull(globalVelocity, "globalVelocity must not be null");
        Objects.requireNonNull(globalAngularVelocity, "globalAngularVelocity must not be null");
        Objects.requireNonNull(globalOrientation, "globalOrientation must not be null");
        Objects.requireNonNull(localPosition, "localPosition must not be null");
        Objects.requireNonNull(localVelocity, "localVelocity must not be null");
        Objects.requireNonNull(localAngularVelocity, "localAngularVelocity must not be null");
        Objects.requireNonNull(localOrientation, "localOrientation must not be null");
    }
}
