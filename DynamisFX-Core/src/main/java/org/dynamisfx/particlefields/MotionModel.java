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
package org.dynamisfx.particlefields;

/**
 * Defines the motion model used by particles in a particle field.
 */
public enum MotionModel {

    /**
     * Keplerian orbital mechanics. Particles orbit indefinitely around a center
     * using semi-major axis, eccentricity, inclination, and angular speed.
     * Used for planetary rings, asteroid belts, nebulae, accretion disks.
     */
    ORBITAL("Orbital", "Keplerian orbital mechanics with indefinite lifetime"),

    /**
     * Linear motion with velocity, acceleration, and drag. Particles have
     * finite lifetimes and are respawned when expired.
     * Used for rain, fire, explosions, swarms, starfields.
     */
    LINEAR("Linear", "Velocity-based motion with finite lifetime");

    private final String displayName;
    private final String description;

    MotionModel(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
