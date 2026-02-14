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
 * Categorizes different types of particle field systems.
 * Each type is associated with a {@link MotionModel} that governs particle behavior.
 */
public enum ParticleFieldType {

    // ===== Orbital types (ported from trips) =====

    /**
     * Planetary ring (Saturn-like): extremely thin, dense, small icy/rocky particles,
     * nearly circular orbits with very low inclination, fast Keplerian rotation.
     */
    PLANETARY_RING("Planetary Ring", MotionModel.ORBITAL),

    /**
     * Asteroid belt (Main belt-like): thick vertical distribution, sparse,
     * large rocky bodies, eccentric and inclined orbits.
     */
    ASTEROID_BELT("Asteroid Belt", MotionModel.ORBITAL),

    /**
     * Debris disk (protoplanetary or collision remnants): moderate thickness,
     * mix of dust and planetesimals, some structure/gaps possible.
     */
    DEBRIS_DISK("Debris Disk", MotionModel.ORBITAL),

    /**
     * Dust cloud / nebula: three-dimensional distribution (not a flat ring),
     * very diffuse, slow turbulent motion rather than orbital.
     */
    DUST_CLOUD("Dust Cloud", MotionModel.ORBITAL),

    /**
     * Accretion disk (around compact objects): thin but very hot/fast,
     * density increases toward center, visible temperature gradient.
     */
    ACCRETION_DISK("Accretion Disk", MotionModel.ORBITAL),

    // ===== Linear types (new) =====

    /**
     * Rain: particles fall from above with gravity and slight wind sway.
     * Short lifetime, blue/gray colors.
     */
    RAIN("Rain", MotionModel.LINEAR),

    /**
     * Fire: particles rise from a base with turbulence, short lifetime.
     * Temperature-based color gradient (yellow to orange to red to dark).
     */
    FIRE("Fire", MotionModel.LINEAR),

    /**
     * Explosion: radial burst from center point with high initial speed
     * and strong drag for deceleration. Short lifetime.
     */
    EXPLOSION("Explosion", MotionModel.LINEAR),

    /**
     * Starfield: static or very slowly drifting particles in spherical volume.
     * Infinite lifetime. White/blue-white colors.
     */
    STARFIELD("Starfield", MotionModel.LINEAR),

    /**
     * Swarm: erratic grouped particles with randomized velocity changes.
     * Moderate lifetime. Particles cluster around a center.
     */
    SWARM("Swarm", MotionModel.LINEAR);

    private final String displayName;
    private final MotionModel motionModel;

    ParticleFieldType(String displayName, MotionModel motionModel) {
        this.displayName = displayName;
        this.motionModel = motionModel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MotionModel getMotionModel() {
        return motionModel;
    }
}
