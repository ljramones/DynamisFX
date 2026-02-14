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
 * Immutable quaternion for orientation state.
 */
public record PhysicsQuaternion(double x, double y, double z, double w) {

    public static final PhysicsQuaternion IDENTITY = new PhysicsQuaternion(0.0, 0.0, 0.0, 1.0);

    public PhysicsQuaternion {
        if (!Double.isFinite(x) || !Double.isFinite(y)
                || !Double.isFinite(z) || !Double.isFinite(w)) {
            throw new IllegalArgumentException("quaternion values must be finite");
        }
    }
}
