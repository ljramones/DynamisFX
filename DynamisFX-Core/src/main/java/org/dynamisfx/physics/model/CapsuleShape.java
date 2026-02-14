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
 * Capsule shape (radius and straight section length in meters).
 */
public record CapsuleShape(double radius, double length) implements PhysicsShape {

    public CapsuleShape {
        if (!(radius > 0.0) || !(length >= 0.0)) {
            throw new IllegalArgumentException("capsule radius must be > 0 and length must be >= 0");
        }
    }
}
