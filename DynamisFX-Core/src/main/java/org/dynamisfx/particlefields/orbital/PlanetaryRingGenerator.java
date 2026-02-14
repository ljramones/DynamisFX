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
 * Generator for planetary rings (Saturn-like).
 *
 * Characteristics:
 * - Extremely thin vertical distribution
 * - Nearly circular orbits (very low eccentricity)
 * - Very low orbital inclination
 * - Dense particle distribution
 * - Fast Keplerian rotation (inner particles faster than outer)
 * - Icy/rocky composition (whites, grays, subtle browns)
 */
public class PlanetaryRingGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double radialRange = config.outerRadius() - config.innerRadius();
        double sizeRange = config.maxSize() - config.minSize();

        for (int i = 0; i < config.numElements(); i++) {
            // Radial distribution - slightly favor middle of ring
            double radialFactor = random.nextGaussian() * 0.3 + 0.5;
            radialFactor = Math.max(0, Math.min(1, radialFactor));
            double semiMajorAxis = config.innerRadius() + radialFactor * radialRange;

            // Very low eccentricity for planetary rings
            double eccentricity = random.nextDouble() * config.maxEccentricity() * 0.1;

            // Very low inclination - rings are flat
            double maxIncRad = Math.toRadians(config.maxInclinationDeg());
            double inclination = (random.nextGaussian() * 0.3) * maxIncRad;

            // Random orbital angles
            double argumentOfPeriapsis = random.nextDouble() * 2 * Math.PI;
            double longitudeOfAscendingNode = random.nextDouble() * 2 * Math.PI;
            double initialAngle = random.nextDouble() * 2 * Math.PI;

            // Keplerian speed: inner particles orbit faster
            double speedFactor = Math.sqrt(config.innerRadius() / semiMajorAxis);
            double angularSpeed = config.baseAngularSpeed() * speedFactor;
            // Small random variation
            angularSpeed *= (0.98 + random.nextDouble() * 0.04);

            // Very thin vertical offset
            double heightOffset = (random.nextGaussian() * 0.5) * config.thickness();

            // Particle size - planetary rings have smaller particles
            double size = config.minSize() + random.nextDouble() * sizeRange;

            // Color variation - icy whites to subtle browns
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());

            elements.add(new ParticleFieldElement(
                    semiMajorAxis, eccentricity, inclination,
                    argumentOfPeriapsis, longitudeOfAscendingNode,
                    initialAngle, angularSpeed, size, heightOffset, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.PLANETARY_RING;
    }

    @Override
    public String getDescription() {
        return "Planetary ring generator - thin, dense, fast-rotating icy/rocky particles";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
