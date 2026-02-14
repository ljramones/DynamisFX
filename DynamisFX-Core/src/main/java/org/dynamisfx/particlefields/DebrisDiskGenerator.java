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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for debris disks (protoplanetary or collision remnants).
 *
 * Characteristics:
 * - Moderate thickness (between planetary ring and asteroid belt)
 * - Mix of circular and slightly eccentric orbits
 * - Moderate inclination
 * - Mix of dust and larger planetesimals
 * - May show density variations (forming structures)
 * - Dusty composition (browns, tans, subtle reds)
 */
public class DebrisDiskGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double radialRange = config.outerRadius() - config.innerRadius();
        double sizeRange = config.maxSize() - config.minSize();

        for (int i = 0; i < config.numElements(); i++) {
            // Radial distribution with some clumping (density waves)
            double baseRadius = random.nextDouble();
            // Add subtle spiral/wave structure
            double waveOffset = 0.05 * Math.sin(baseRadius * 8 * Math.PI + random.nextDouble() * Math.PI);
            double radialFactor = Math.max(0, Math.min(1, baseRadius + waveOffset));
            double semiMajorAxis = config.innerRadius() + radialFactor * radialRange;

            // Mix of circular and eccentric orbits
            double eccentricity;
            if (random.nextDouble() < 0.6) {
                // Mostly circular (dust settles)
                eccentricity = random.nextDouble() * config.maxEccentricity() * 0.3;
            } else {
                // Some eccentric (recent collisions)
                eccentricity = random.nextDouble() * config.maxEccentricity();
            }

            // Moderate inclination
            double maxIncRad = Math.toRadians(config.maxInclinationDeg());
            double inclination = random.nextGaussian() * 0.4 * maxIncRad;

            // Random orbital angles
            double argumentOfPeriapsis = random.nextDouble() * 2 * Math.PI;
            double longitudeOfAscendingNode = random.nextDouble() * 2 * Math.PI;
            double initialAngle = random.nextDouble() * 2 * Math.PI;

            // Keplerian speed
            double speedFactor = Math.sqrt(config.innerRadius() / semiMajorAxis);
            double angularSpeed = config.baseAngularSpeed() * speedFactor;
            angularSpeed *= (0.9 + random.nextDouble() * 0.2);

            // Moderate vertical distribution
            double heightOffset = random.nextGaussian() * 0.4 * config.thickness();

            // Bimodal size distribution - lots of dust, some larger bodies
            double size;
            if (random.nextDouble() < 0.8) {
                // Dust particles (small)
                size = config.minSize() + random.nextDouble() * sizeRange * 0.3;
            } else {
                // Planetesimals (larger)
                size = config.minSize() + (0.5 + random.nextDouble() * 0.5) * sizeRange;
            }

            // Color - dusty browns and tans
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
        return ParticleFieldType.DEBRIS_DISK;
    }

    @Override
    public String getDescription() {
        return "Debris disk generator - moderate thickness, dusty with planetesimals";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
