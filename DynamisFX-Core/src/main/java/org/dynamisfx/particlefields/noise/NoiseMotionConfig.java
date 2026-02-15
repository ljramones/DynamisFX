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
package org.dynamisfx.particlefields.noise;

/**
 * Configuration for noise-driven particle motion.
 * Controls curl-noise displacement strength, density modulation, and update frequency.
 *
 * <p>Use the static presets {@link #fog()}, {@link #clouds()}, {@link #smoke()}
 * for common configurations, or create a custom config via {@link #builder()}.
 */
public record NoiseMotionConfig(
        double curlStrength,
        double curlFrequency,
        int curlOctaves,
        double timeSpeed,
        int updateInterval,
        boolean densityOpacityEnabled,
        double densityFrequency,
        int densityOctaves,
        double densityMinOpacity,
        double densityMaxOpacity,
        boolean densityScaleEnabled,
        double densityMinScale,
        double densityMaxScale,
        int seed
) {

    /**
     * Fog preset: slow, large-scale curl with high density variation.
     */
    public static NoiseMotionConfig fog() {
        return builder()
                .curlStrength(0.3)
                .curlFrequency(0.015)
                .curlOctaves(3)
                .timeSpeed(0.2)
                .updateInterval(3)
                .densityOpacityEnabled(true)
                .densityFrequency(0.02)
                .densityOctaves(3)
                .densityMinOpacity(0.1)
                .densityMaxOpacity(0.6)
                .densityScaleEnabled(true)
                .densityMinScale(0.5)
                .densityMaxScale(1.5)
                .seed(42)
                .build();
    }

    /**
     * Clouds preset: very slow, large-scale rolling motion.
     */
    public static NoiseMotionConfig clouds() {
        return builder()
                .curlStrength(0.15)
                .curlFrequency(0.008)
                .curlOctaves(4)
                .timeSpeed(0.1)
                .updateInterval(4)
                .densityOpacityEnabled(true)
                .densityFrequency(0.01)
                .densityOctaves(4)
                .densityMinOpacity(0.3)
                .densityMaxOpacity(0.9)
                .densityScaleEnabled(true)
                .densityMinScale(0.7)
                .densityMaxScale(2.0)
                .seed(42)
                .build();
    }

    /**
     * Smoke preset: fast, turbulent curl with rapid density changes.
     */
    public static NoiseMotionConfig smoke() {
        return builder()
                .curlStrength(0.6)
                .curlFrequency(0.03)
                .curlOctaves(3)
                .timeSpeed(0.5)
                .updateInterval(2)
                .densityOpacityEnabled(true)
                .densityFrequency(0.04)
                .densityOctaves(2)
                .densityMinOpacity(0.05)
                .densityMaxOpacity(0.5)
                .densityScaleEnabled(true)
                .densityMinScale(0.3)
                .densityMaxScale(1.8)
                .seed(42)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private double curlStrength = 0.3;
        private double curlFrequency = 0.02;
        private int curlOctaves = 3;
        private double timeSpeed = 0.3;
        private int updateInterval = 3;
        private boolean densityOpacityEnabled = false;
        private double densityFrequency = 0.02;
        private int densityOctaves = 3;
        private double densityMinOpacity = 0.1;
        private double densityMaxOpacity = 1.0;
        private boolean densityScaleEnabled = false;
        private double densityMinScale = 0.5;
        private double densityMaxScale = 1.5;
        private int seed = 42;

        private Builder() {}

        public Builder curlStrength(double v) { this.curlStrength = v; return this; }
        public Builder curlFrequency(double v) { this.curlFrequency = v; return this; }
        public Builder curlOctaves(int v) { this.curlOctaves = v; return this; }
        public Builder timeSpeed(double v) { this.timeSpeed = v; return this; }
        public Builder updateInterval(int v) { this.updateInterval = Math.max(1, v); return this; }
        public Builder densityOpacityEnabled(boolean v) { this.densityOpacityEnabled = v; return this; }
        public Builder densityFrequency(double v) { this.densityFrequency = v; return this; }
        public Builder densityOctaves(int v) { this.densityOctaves = v; return this; }
        public Builder densityMinOpacity(double v) { this.densityMinOpacity = v; return this; }
        public Builder densityMaxOpacity(double v) { this.densityMaxOpacity = v; return this; }
        public Builder densityScaleEnabled(boolean v) { this.densityScaleEnabled = v; return this; }
        public Builder densityMinScale(double v) { this.densityMinScale = v; return this; }
        public Builder densityMaxScale(double v) { this.densityMaxScale = v; return this; }
        public Builder seed(int v) { this.seed = v; return this; }

        public NoiseMotionConfig build() {
            return new NoiseMotionConfig(
                    curlStrength, curlFrequency, curlOctaves,
                    timeSpeed, updateInterval,
                    densityOpacityEnabled, densityFrequency, densityOctaves,
                    densityMinOpacity, densityMaxOpacity,
                    densityScaleEnabled, densityMinScale, densityMaxScale,
                    seed
            );
        }
    }
}
