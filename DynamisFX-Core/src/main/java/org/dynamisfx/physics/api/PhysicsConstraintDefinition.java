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
 * Constraint creation definition for physics worlds.
 */
public record PhysicsConstraintDefinition(
        PhysicsConstraintType type,
        PhysicsBodyHandle bodyA,
        PhysicsBodyHandle bodyB,
        PhysicsVector3 anchorWorld,
        PhysicsVector3 axisWorld,
        Double lowerLimit,
        Double upperLimit) {

    public PhysicsConstraintDefinition(
            PhysicsConstraintType type,
            PhysicsBodyHandle bodyA,
            PhysicsBodyHandle bodyB,
            PhysicsVector3 anchorWorld) {
        this(type, bodyA, bodyB, anchorWorld, null, null, null);
    }

    public PhysicsConstraintDefinition {
        if (type == null || bodyA == null || bodyB == null) {
            throw new IllegalArgumentException("type, bodyA and bodyB must not be null");
        }
        if ((type == PhysicsConstraintType.HINGE || type == PhysicsConstraintType.SLIDER) && axisWorld == null) {
            throw new IllegalArgumentException("axisWorld is required for hinge/slider constraints");
        }
        if (lowerLimit != null && !Double.isFinite(lowerLimit)) {
            throw new IllegalArgumentException("lowerLimit must be finite when provided");
        }
        if (upperLimit != null && !Double.isFinite(upperLimit)) {
            throw new IllegalArgumentException("upperLimit must be finite when provided");
        }
        if (lowerLimit != null && upperLimit != null && lowerLimit > upperLimit) {
            throw new IllegalArgumentException("lowerLimit must be <= upperLimit");
        }
    }
}
