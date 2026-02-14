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

import javafx.scene.paint.Color;

/**
 * Configuration parameters for a particle field system.
 * Supports both orbital and linear motion models.
 * <p>
 * Orbital parameters (innerRadius, outerRadius, eccentricity, etc.) are used
 * by orbital generators. Linear parameters (gravity, wind, drag, speed, etc.)
 * are used by linear generators.
 */
public record ParticleFieldConfiguration(
        /** The type of particle field system */
        ParticleFieldType type,

        /** Inner radius of the field (visual units) */
        double innerRadius,

        /** Outer radius of the field (visual units) */
        double outerRadius,

        /** Number of particles/elements in the field */
        int numElements,

        /** Minimum particle size */
        double minSize,

        /** Maximum particle size */
        double maxSize,

        /** Vertical thickness/spread of the field (visual units) */
        double thickness,

        /** Maximum orbital inclination in degrees (0 = perfectly flat) */
        double maxInclinationDeg,

        /** Maximum orbital eccentricity (0 = circular, <1 = elliptical) */
        double maxEccentricity,

        /** Base angular speed multiplier (affects rotation rate) */
        double baseAngularSpeed,

        /** Radius of the central body */
        double centralBodyRadius,

        /** Primary color for particles */
        Color primaryColor,

        /** Secondary color for particles (for gradients/variation) */
        Color secondaryColor,

        /** Tertiary color for multi-zone gradients (middle color band) */
        Color tertiaryColor,

        /** Color gradient mode */
        ColorGradientMode colorGradientMode,

        /** Display name */
        String name,

        // ==================== Nebula-Specific Parameters ====================

        /** Radial power for density falloff. < 0.5 = dense core, > 0.5 = shell-like */
        double radialPower,

        /** Noise strength for filamentary structure (0.0 - 1.0) */
        double noiseStrength,

        /** Number of noise octaves for detail level (1-6) */
        int noiseOctaves,

        /** Noise persistence (amplitude decay per octave, 0.0-1.0) */
        double noisePersistence,

        /** Noise lacunarity (frequency multiplier per octave, 1.0-3.0) */
        double noiseLacunarity,

        /** Anisotropic factors for filament stretching [x, y, z] */
        double[] filamentAnisotropy,

        /** Random seed for reproducible procedural generation */
        long seed,

        // ==================== Glow Parameters ====================

        /** Whether glow effect is enabled */
        boolean glowEnabled,

        /** Glow intensity (0.0 - 1.0) */
        double glowIntensity,

        // ==================== Linear Motion Parameters ====================

        /** Acceleration vector (e.g. gravity), default {0, -9.8, 0} */
        double[] gravity,

        /** Constant wind force vector, default {0, 0, 0} */
        double[] wind,

        /** Velocity damping factor for linear particles (0.0 = none) */
        double drag,

        /** Minimum particle lifetime in seconds (-1 = infinite) */
        double minLifetime,

        /** Maximum particle lifetime in seconds (-1 = infinite) */
        double maxLifetime,

        /** Minimum initial speed for linear particles */
        double minSpeed,

        /** Maximum initial speed for linear particles */
        double maxSpeed,

        /** Emission cone half-angle in degrees (0 = directional) */
        double spreadAngle,

        /** Emitter dimensions {width, height, depth}, default {0,0,0} (point) */
        double[] emitterSize
) {
    /**
     * Builder for creating ParticleFieldConfiguration instances with defaults.
     */
    public static class Builder {
        // Orbital defaults
        private ParticleFieldType type = ParticleFieldType.ASTEROID_BELT;
        private double innerRadius = 50;
        private double outerRadius = 100;
        private int numElements = 5000;
        private double minSize = 0.5;
        private double maxSize = 2.0;
        private double thickness = 5.0;
        private double maxInclinationDeg = 10.0;
        private double maxEccentricity = 0.05;
        private double baseAngularSpeed = 0.002;
        private double centralBodyRadius = 8.0;
        private Color primaryColor = Color.LIGHTGRAY;
        private Color secondaryColor = Color.DARKGRAY;
        private Color tertiaryColor = Color.GRAY;
        private ColorGradientMode colorGradientMode = ColorGradientMode.LINEAR;
        private String name = "Particle Field";

        // Nebula-specific defaults
        private double radialPower = 0.5;
        private double noiseStrength = 0.0;
        private int noiseOctaves = 3;
        private double noisePersistence = 0.5;
        private double noiseLacunarity = 2.2;
        private double[] filamentAnisotropy = new double[]{1.0, 1.0, 1.0};
        private long seed = System.currentTimeMillis();

        // Glow defaults
        private boolean glowEnabled = false;
        private double glowIntensity = 0.0;

        // Linear motion defaults
        private double[] gravity = new double[]{0, -9.8, 0};
        private double[] wind = new double[]{0, 0, 0};
        private double drag = 0.0;
        private double minLifetime = -1;
        private double maxLifetime = -1;
        private double minSpeed = 0;
        private double maxSpeed = 0;
        private double spreadAngle = 0;
        private double[] emitterSize = new double[]{0, 0, 0};

        public Builder type(ParticleFieldType type) { this.type = type; return this; }
        public Builder innerRadius(double innerRadius) { this.innerRadius = innerRadius; return this; }
        public Builder outerRadius(double outerRadius) { this.outerRadius = outerRadius; return this; }
        public Builder numElements(int numElements) { this.numElements = numElements; return this; }
        public Builder minSize(double minSize) { this.minSize = minSize; return this; }
        public Builder maxSize(double maxSize) { this.maxSize = maxSize; return this; }
        public Builder thickness(double thickness) { this.thickness = thickness; return this; }
        public Builder maxInclinationDeg(double maxInclinationDeg) { this.maxInclinationDeg = maxInclinationDeg; return this; }
        public Builder maxEccentricity(double maxEccentricity) { this.maxEccentricity = maxEccentricity; return this; }
        public Builder baseAngularSpeed(double baseAngularSpeed) { this.baseAngularSpeed = baseAngularSpeed; return this; }
        public Builder centralBodyRadius(double centralBodyRadius) { this.centralBodyRadius = centralBodyRadius; return this; }
        public Builder primaryColor(Color primaryColor) { this.primaryColor = primaryColor; return this; }
        public Builder secondaryColor(Color secondaryColor) { this.secondaryColor = secondaryColor; return this; }
        public Builder tertiaryColor(Color tertiaryColor) { this.tertiaryColor = tertiaryColor; return this; }
        public Builder colorGradientMode(ColorGradientMode mode) { this.colorGradientMode = mode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder radialPower(double radialPower) { this.radialPower = radialPower; return this; }
        public Builder noiseStrength(double noiseStrength) { this.noiseStrength = noiseStrength; return this; }
        public Builder noiseOctaves(int noiseOctaves) { this.noiseOctaves = noiseOctaves; return this; }
        public Builder noisePersistence(double persistence) { this.noisePersistence = persistence; return this; }
        public Builder noiseLacunarity(double lacunarity) { this.noiseLacunarity = lacunarity; return this; }
        public Builder filamentAnisotropy(double[] anisotropy) {
            if (anisotropy != null && anisotropy.length == 3) {
                this.filamentAnisotropy = anisotropy.clone();
            }
            return this;
        }
        public Builder seed(long seed) { this.seed = seed; return this; }
        public Builder glowEnabled(boolean enabled) { this.glowEnabled = enabled; return this; }
        public Builder glowIntensity(double intensity) { this.glowIntensity = intensity; return this; }
        public Builder gravity(double[] gravity) {
            if (gravity != null && gravity.length == 3) {
                this.gravity = gravity.clone();
            }
            return this;
        }
        public Builder wind(double[] wind) {
            if (wind != null && wind.length == 3) {
                this.wind = wind.clone();
            }
            return this;
        }
        public Builder drag(double drag) { this.drag = drag; return this; }
        public Builder minLifetime(double minLifetime) { this.minLifetime = minLifetime; return this; }
        public Builder maxLifetime(double maxLifetime) { this.maxLifetime = maxLifetime; return this; }
        public Builder minSpeed(double minSpeed) { this.minSpeed = minSpeed; return this; }
        public Builder maxSpeed(double maxSpeed) { this.maxSpeed = maxSpeed; return this; }
        public Builder spreadAngle(double spreadAngle) { this.spreadAngle = spreadAngle; return this; }
        public Builder emitterSize(double[] emitterSize) {
            if (emitterSize != null && emitterSize.length == 3) {
                this.emitterSize = emitterSize.clone();
            }
            return this;
        }

        public ParticleFieldConfiguration build() {
            return new ParticleFieldConfiguration(
                    type, innerRadius, outerRadius, numElements,
                    minSize, maxSize, thickness, maxInclinationDeg,
                    maxEccentricity, baseAngularSpeed, centralBodyRadius,
                    primaryColor, secondaryColor, tertiaryColor, colorGradientMode, name,
                    radialPower, noiseStrength, noiseOctaves,
                    noisePersistence, noiseLacunarity, filamentAnisotropy,
                    seed, glowEnabled, glowIntensity,
                    gravity, wind, drag, minLifetime, maxLifetime,
                    minSpeed, maxSpeed, spreadAngle, emitterSize
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a copy of this configuration with a new seed.
     */
    public ParticleFieldConfiguration withSeed(long newSeed) {
        return new ParticleFieldConfiguration(
                type, innerRadius, outerRadius, numElements,
                minSize, maxSize, thickness, maxInclinationDeg,
                maxEccentricity, baseAngularSpeed, centralBodyRadius,
                primaryColor, secondaryColor, tertiaryColor, colorGradientMode, name,
                radialPower, noiseStrength, noiseOctaves,
                noisePersistence, noiseLacunarity, filamentAnisotropy,
                newSeed, glowEnabled, glowIntensity,
                gravity, wind, drag, minLifetime, maxLifetime,
                minSpeed, maxSpeed, spreadAngle, emitterSize
        );
    }

    /**
     * Returns a copy with adjusted particle count (for LOD).
     */
    public ParticleFieldConfiguration withNumElements(int newNumElements) {
        return new ParticleFieldConfiguration(
                type, innerRadius, outerRadius, newNumElements,
                minSize, maxSize, thickness, maxInclinationDeg,
                maxEccentricity, baseAngularSpeed, centralBodyRadius,
                primaryColor, secondaryColor, tertiaryColor, colorGradientMode, name,
                radialPower, noiseStrength, noiseOctaves,
                noisePersistence, noiseLacunarity, filamentAnisotropy,
                seed, glowEnabled, glowIntensity,
                gravity, wind, drag, minLifetime, maxLifetime,
                minSpeed, maxSpeed, spreadAngle, emitterSize
        );
    }

    /**
     * Returns a copy with glow settings adjusted.
     */
    public ParticleFieldConfiguration withGlow(boolean enabled, double intensity) {
        return new ParticleFieldConfiguration(
                type, innerRadius, outerRadius, numElements,
                minSize, maxSize, thickness, maxInclinationDeg,
                maxEccentricity, baseAngularSpeed, centralBodyRadius,
                primaryColor, secondaryColor, tertiaryColor, colorGradientMode, name,
                radialPower, noiseStrength, noiseOctaves,
                noisePersistence, noiseLacunarity, filamentAnisotropy,
                seed, enabled, intensity,
                gravity, wind, drag, minLifetime, maxLifetime,
                minSpeed, maxSpeed, spreadAngle, emitterSize
        );
    }

    /**
     * Returns a copy with adjusted noise octaves.
     */
    public ParticleFieldConfiguration withNoiseOctaves(int newOctaves) {
        return new ParticleFieldConfiguration(
                type, innerRadius, outerRadius, numElements,
                minSize, maxSize, thickness, maxInclinationDeg,
                maxEccentricity, baseAngularSpeed, centralBodyRadius,
                primaryColor, secondaryColor, tertiaryColor, colorGradientMode, name,
                radialPower, noiseStrength, newOctaves,
                noisePersistence, noiseLacunarity, filamentAnisotropy,
                seed, glowEnabled, glowIntensity,
                gravity, wind, drag, minLifetime, maxLifetime,
                minSpeed, maxSpeed, spreadAngle, emitterSize
        );
    }

    /**
     * Returns a copy with adjusted noise strength.
     */
    public ParticleFieldConfiguration withNoiseStrength(double newStrength) {
        return new ParticleFieldConfiguration(
                type, innerRadius, outerRadius, numElements,
                minSize, maxSize, thickness, maxInclinationDeg,
                maxEccentricity, baseAngularSpeed, centralBodyRadius,
                primaryColor, secondaryColor, tertiaryColor, colorGradientMode, name,
                radialPower, newStrength, noiseOctaves,
                noisePersistence, noiseLacunarity, filamentAnisotropy,
                seed, glowEnabled, glowIntensity,
                gravity, wind, drag, minLifetime, maxLifetime,
                minSpeed, maxSpeed, spreadAngle, emitterSize
        );
    }
}
