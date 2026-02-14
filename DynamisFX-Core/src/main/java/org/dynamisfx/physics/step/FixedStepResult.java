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

package org.dynamisfx.physics.step;

/**
 * Result of a fixed-step accumulator advance.
 */
public record FixedStepResult(
        int stepsExecuted,
        double interpolationAlpha,
        double accumulatedRemainderSeconds) {

    public FixedStepResult {
        if (stepsExecuted < 0) {
            throw new IllegalArgumentException("stepsExecuted must be >= 0");
        }
        if (!Double.isFinite(interpolationAlpha) || interpolationAlpha < 0.0 || interpolationAlpha > 1.0) {
            throw new IllegalArgumentException("interpolationAlpha must be finite in [0,1]");
        }
        if (!Double.isFinite(accumulatedRemainderSeconds) || accumulatedRemainderSeconds < 0.0) {
            throw new IllegalArgumentException("accumulatedRemainderSeconds must be finite and >= 0");
        }
    }
}
