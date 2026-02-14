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
 * Sphere overlap query in world coordinates.
 */
public record OverlapSphereQuery(
        PhysicsVector3 center,
        double radiusMeters,
        int maxResults) {

    public OverlapSphereQuery {
        if (center == null) {
            throw new IllegalArgumentException("center must not be null");
        }
        if (!Double.isFinite(radiusMeters) || radiusMeters <= 0.0) {
            throw new IllegalArgumentException("radiusMeters must be finite and > 0");
        }
        if (maxResults <= 0) {
            throw new IllegalArgumentException("maxResults must be > 0");
        }
    }
}
