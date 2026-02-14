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

package org.dynamisfx.physics.api;

import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * World-space raycast request.
 */
public record RaycastRequest(
        PhysicsVector3 origin,
        PhysicsVector3 direction,
        double maxDistanceMeters) {

    public RaycastRequest {
        if (origin == null || direction == null) {
            throw new IllegalArgumentException("origin and direction must not be null");
        }
        if (!Double.isFinite(maxDistanceMeters) || maxDistanceMeters <= 0.0) {
            throw new IllegalArgumentException("maxDistanceMeters must be finite and > 0");
        }
        double n2 = (direction.x() * direction.x()) + (direction.y() * direction.y()) + (direction.z() * direction.z());
        if (!(n2 > 0.0)) {
            throw new IllegalArgumentException("direction norm must be > 0");
        }
    }
}
