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
 * World-level simulation configuration.
 */
public record PhysicsWorldConfiguration(
        ReferenceFrame referenceFrame,
        PhysicsVector3 gravity,
        double fixedStepSeconds,
        PhysicsRuntimeTuning runtimeTuning) {

    public static final int DEFAULT_SOLVER_ITERATIONS = 20;
    public static final double DEFAULT_CONTACT_FRICTION = Double.POSITIVE_INFINITY;
    public static final double DEFAULT_CONTACT_BOUNCE = 0.1;
    public static final double DEFAULT_CONTACT_SOFT_CFM = 1e-5;
    public static final double DEFAULT_CONTACT_BOUNCE_VELOCITY = 0.1;

    public PhysicsWorldConfiguration(
            ReferenceFrame referenceFrame,
            PhysicsVector3 gravity,
            double fixedStepSeconds) {
        this(
                referenceFrame,
                gravity,
                fixedStepSeconds,
                new PhysicsRuntimeTuning(
                        DEFAULT_SOLVER_ITERATIONS,
                        DEFAULT_CONTACT_FRICTION,
                        DEFAULT_CONTACT_BOUNCE,
                        DEFAULT_CONTACT_SOFT_CFM,
                        DEFAULT_CONTACT_BOUNCE_VELOCITY));
    }

    public PhysicsWorldConfiguration(
            ReferenceFrame referenceFrame,
            PhysicsVector3 gravity,
            double fixedStepSeconds,
            int solverIterations,
            double contactFriction,
            double contactBounce,
            double contactSoftCfm,
            double contactBounceVelocity) {
        this(
                referenceFrame,
                gravity,
                fixedStepSeconds,
                new PhysicsRuntimeTuning(
                        solverIterations,
                        contactFriction,
                        contactBounce,
                        contactSoftCfm,
                        contactBounceVelocity));
    }

    public PhysicsWorldConfiguration {
        if (referenceFrame == null || gravity == null) {
            throw new IllegalArgumentException("referenceFrame and gravity must not be null");
        }
        if (!(fixedStepSeconds > 0.0) || !Double.isFinite(fixedStepSeconds)) {
            throw new IllegalArgumentException("fixedStepSeconds must be > 0 and finite");
        }
        if (runtimeTuning == null) {
            throw new IllegalArgumentException("runtimeTuning must not be null");
        }
    }
}
