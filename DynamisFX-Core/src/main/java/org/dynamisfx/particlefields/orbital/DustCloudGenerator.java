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
package org.dynamisfx.particlefields.orbital;

import javafx.scene.paint.Color;
import org.dynamisfx.particlefields.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for dust clouds / nebulae.
 * <p>
 * Characteristics:
 * - Three-dimensional distribution (spherical/ellipsoidal, not a flat ring)
 * - Configurable density falloff (dense core vs shell-like)
 * - Noise-based filamentary structure for realistic appearance
 * - Core-biased color and opacity gradients
 * - Slow, turbulent motion rather than organized orbital motion
 * - Very small particles (dust/gas)
 * - Colorful (emission/reflection nebulae) or dark
 * - No clear orbital plane
 */
public class DustCloudGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double radialRange = config.outerRadius() - config.innerRadius();
        double sizeRange = config.maxSize() - config.minSize();

        // Create noise generator with configurable parameters from config
        NoiseGenerator noise = new NoiseGenerator(
                config.seed(),
                config.noisePersistence(),
                config.noiseLacunarity()
        );

        // Noise scale based on nebula size (larger nebulae need coarser noise)
        double noiseScale = 0.8 / Math.max(1, radialRange * 0.01);

        for (int i = 0; i < config.numElements(); i++) {
            ParticleFieldElement element = generateParticle(
                    config, random, noise,
                    radialRange, sizeRange, noiseScale
            );
            if (element != null) {
                elements.add(element);
            }
        }

        return elements;
    }

    private ParticleFieldElement generateParticle(ParticleFieldConfiguration config, Random random,
                                                    NoiseGenerator noise,
                                                    double radialRange, double sizeRange,
                                                    double noiseScale) {
        // === RADIAL DISTRIBUTION ===
        double u = random.nextDouble();
        double r = config.innerRadius() + radialRange * Math.pow(u, config.radialPower());
        r = Math.max(config.innerRadius(), Math.min(config.outerRadius(), r));

        // === ANGULAR DISTRIBUTION ===
        double theta = random.nextDouble() * 2 * Math.PI;
        double phi = Math.acos(2 * random.nextDouble() - 1);

        // Convert spherical to Cartesian for noise sampling
        double px = r * Math.sin(phi) * Math.cos(theta);
        double py = r * Math.sin(phi) * Math.sin(theta);
        double pz = r * Math.cos(phi);

        // === NOISE DISPLACEMENT ===
        if (config.noiseStrength() > 0) {
            double[] anisotropy = config.filamentAnisotropy();
            if (anisotropy == null || anisotropy.length != 3) {
                anisotropy = new double[]{1.0, 0.7, 0.4};
            }

            double[] displacement = noise.multiScaleFilamentDisplacement(
                    px * noiseScale,
                    py * noiseScale,
                    pz * noiseScale,
                    config.noiseOctaves(),
                    radialRange * 0.15 * config.noiseStrength(),
                    0.7, 0.3,
                    anisotropy
            );

            px += displacement[0];
            py += displacement[1];
            pz += displacement[2];

            r = Math.sqrt(px * px + py * py + pz * pz);
            r = Math.max(config.innerRadius(), Math.min(config.outerRadius() * 1.1, r));
        }

        // Convert back to pseudo-orbital elements
        double newTheta = Math.atan2(py, px);
        double newR = Math.sqrt(px * px + py * py);
        double newPhi = Math.atan2(newR, pz);

        double inclination = newPhi - Math.PI / 2;
        double longitudeOfAscendingNode = newTheta;
        double eccentricity = random.nextDouble() * 0.02;
        double argumentOfPeriapsis = random.nextDouble() * 2 * Math.PI;
        double initialAngle = random.nextDouble() * 2 * Math.PI;

        // Very slow, almost random motion
        double angularSpeed = config.baseAngularSpeed() * 0.1 * (0.5 + random.nextDouble());
        if (random.nextBoolean()) {
            angularSpeed = -angularSpeed;
        }

        double heightOffset = random.nextGaussian() * config.thickness() * 0.3;

        // Size - smaller near edges
        double coreFactor = 1.0 - (r - config.innerRadius()) / radialRange;
        coreFactor = Math.max(0, Math.min(1, coreFactor));
        double size = config.minSize() + random.nextDouble() * sizeRange * (0.3 + 0.2 * coreFactor);

        Color color = calculateColor(config, random, noise, px, py, pz, r, radialRange, coreFactor);

        return new ParticleFieldElement(
                r, eccentricity, inclination,
                argumentOfPeriapsis, longitudeOfAscendingNode,
                initialAngle, angularSpeed, size, heightOffset, color
        );
    }

    private Color calculateColor(ParticleFieldConfiguration config, Random random,
                                   NoiseGenerator noise,
                                   double px, double py, double pz,
                                   double r, double radialRange,
                                   double coreFactor) {
        double colorFactor = calculateColorFactor(config, random, noise, px, py, pz, r, radialRange, coreFactor);

        Color base = interpolateColor(config.primaryColor(), config.secondaryColor(), colorFactor);

        // Core-biased brightness
        double brightnessBoost = 1.0 + coreFactor * 0.4;
        double saturationBoost = 1.0 + coreFactor * 0.3;

        base = base.deriveColor(0, saturationBoost, brightnessBoost, 1.0);

        // Noise-based color variation
        if (config.noiseStrength() > 0) {
            double colorNoise = noise.layeredNoise(px * 0.5, py * 0.5, pz * 0.5, 2);
            double hueShift = colorNoise * 10 * config.noiseStrength();
            base = base.deriveColor(hueShift, 1.0, 1.0, 1.0);
        }

        // Opacity
        double baseOpacity = 0.3 + coreFactor * 0.4;
        double opacityVariation = random.nextDouble() * 0.3;
        double opacity = Math.min(1.0, baseOpacity + opacityVariation);

        if (config.noiseStrength() > 0) {
            double opacityNoise = (noise.layeredNoise(px * 0.3, py * 0.3, pz * 0.3, 2) + 1) * 0.5;
            opacity *= (0.7 + opacityNoise * 0.3);
        }

        return Color.color(
                base.getRed(),
                base.getGreen(),
                base.getBlue(),
                Math.max(0.1, Math.min(1.0, opacity))
        );
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.DUST_CLOUD;
    }

    @Override
    public String getDescription() {
        return "Dust cloud generator - 3D diffuse distribution with " +
               "configurable density falloff, noise-based filaments, and core-biased gradients";
    }

    private double calculateColorFactor(ParticleFieldConfiguration config, Random random,
                                         NoiseGenerator noise,
                                         double px, double py, double pz,
                                         double r, double radialRange,
                                         double coreFactor) {
        ColorGradientMode mode = config.colorGradientMode() != null ?
                config.colorGradientMode() : ColorGradientMode.LINEAR;

        switch (mode) {
            case LINEAR:
                double colorFactor = random.nextDouble();
                return colorFactor * (1.0 - coreFactor * 0.3);
            case RADIAL:
                return 1.0 - coreFactor;
            case NOISE_BASED:
                double noiseValue = noise.layeredNoise(px * 0.5, py * 0.5, pz * 0.5, 2);
                return (noiseValue + 1.0) * 0.5;
            case TEMPERATURE:
                return 1.0 - coreFactor;
            case MULTI_ZONE:
                return 1.0 - coreFactor;
            default:
                return random.nextDouble();
        }
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        t = Math.max(0, Math.min(1, t));
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
