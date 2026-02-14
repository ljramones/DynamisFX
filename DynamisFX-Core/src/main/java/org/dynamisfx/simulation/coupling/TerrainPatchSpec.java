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

/**
 * Tiling parameters for a local terrain patch.
 */
public record TerrainPatchSpec(
        double halfExtentMeters,
        double tileSizeMeters,
        double tileThicknessMeters) {

    public TerrainPatchSpec {
        if (!Double.isFinite(halfExtentMeters) || halfExtentMeters <= 0.0) {
            throw new IllegalArgumentException("halfExtentMeters must be finite and > 0");
        }
        if (!Double.isFinite(tileSizeMeters) || tileSizeMeters <= 0.0) {
            throw new IllegalArgumentException("tileSizeMeters must be finite and > 0");
        }
        if (!Double.isFinite(tileThicknessMeters) || tileThicknessMeters <= 0.0) {
            throw new IllegalArgumentException("tileThicknessMeters must be finite and > 0");
        }
    }
}
