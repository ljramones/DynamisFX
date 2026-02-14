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
 * Runtime-tunable solver/contact parameters.
 */
public record PhysicsRuntimeTuning(
        int solverIterations,
        double contactFriction,
        double contactBounce,
        double contactSoftCfm,
        double contactBounceVelocity) {

    public PhysicsRuntimeTuning {
        if (solverIterations < 1) {
            throw new IllegalArgumentException("solverIterations must be >= 1");
        }
        if (!(contactFriction >= 0.0) || Double.isNaN(contactFriction)) {
            throw new IllegalArgumentException("contactFriction must be >= 0 or infinity");
        }
        if (!(contactBounce >= 0.0 && contactBounce <= 1.0) || !Double.isFinite(contactBounce)) {
            throw new IllegalArgumentException("contactBounce must be finite in [0,1]");
        }
        if (!(contactSoftCfm >= 0.0) || !Double.isFinite(contactSoftCfm)) {
            throw new IllegalArgumentException("contactSoftCfm must be finite and >= 0");
        }
        if (!(contactBounceVelocity >= 0.0) || !Double.isFinite(contactBounceVelocity)) {
            throw new IllegalArgumentException("contactBounceVelocity must be finite and >= 0");
        }
    }
}
