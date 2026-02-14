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

import java.util.List;
import org.dynamisfx.physics.api.PhysicsBodyHandle;

/**
 * Summary of spawned terrain tile bodies.
 */
public record TerrainPatchSpawnResult(
        int tileCount,
        List<PhysicsBodyHandle> tileHandles) {

    public TerrainPatchSpawnResult {
        if (tileCount < 0) {
            throw new IllegalArgumentException("tileCount must be >= 0");
        }
        if (tileHandles == null) {
            throw new IllegalArgumentException("tileHandles must not be null");
        }
    }
}
