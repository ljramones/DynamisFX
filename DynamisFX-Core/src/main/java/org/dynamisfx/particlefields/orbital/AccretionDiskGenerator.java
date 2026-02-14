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
 * Generator for accretion disks (around compact objects like black holes, neutron stars).
 *
 * Characteristics:
 * - Thin disk structure
 * - Very fast rotation, especially near center
 * - Density increases toward center
 * - Temperature gradient (hot inner edge, cooler outer)
 * - Nearly circular orbits (viscosity circularizes)
 * - Very low inclination (disk is flat)
 * - Color gradient from blue-white (hot) to red (cooler)
 */
public class AccretionDiskGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double radialRange = config.outerRadius() - config.innerRadius();
        double sizeRange = config.maxSize() - config.minSize();

        for (int i = 0; i < config.numElements(); i++) {
            // Radial distribution - denser toward center (power law)
            double u = random.nextDouble();
            double radialFactor = Math.pow(u, 0.5);
            double semiMajorAxis = config.innerRadius() + radialFactor * radialRange;

            // Very low eccentricity - viscosity circularizes orbits
            double eccentricity = random.nextDouble() * config.maxEccentricity() * 0.05;

            // Very flat disk
            double maxIncRad = Math.toRadians(config.maxInclinationDeg());
            double inclination = random.nextGaussian() * 0.1 * maxIncRad;

            // Random orbital angles
            double argumentOfPeriapsis = random.nextDouble() * 2 * Math.PI;
            double longitudeOfAscendingNode = random.nextDouble() * 2 * Math.PI;
            double initialAngle = random.nextDouble() * 2 * Math.PI;

            // Very fast Keplerian speed
            double speedFactor = Math.sqrt(config.innerRadius() / semiMajorAxis);
            double angularSpeed = config.baseAngularSpeed() * speedFactor * 2.0;
            angularSpeed *= (0.95 + random.nextDouble() * 0.1);

            // Very thin vertical distribution with slight outer flare
            double heightOffset = random.nextGaussian() * 0.2 * config.thickness();
            double flare = (semiMajorAxis - config.innerRadius()) / radialRange;
            heightOffset *= (1 + flare * 0.5);

            // Smaller particles (gas/plasma)
            double size = config.minSize() + random.nextDouble() * sizeRange * 0.6;

            // Temperature-based color gradient
            double temperatureFactor = 1.0 - radialFactor;
            Color color = getTemperatureColor(temperatureFactor, config.primaryColor(), config.secondaryColor());

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
        return ParticleFieldType.ACCRETION_DISK;
    }

    @Override
    public String getDescription() {
        return "Accretion disk generator - thin, fast, temperature gradient toward center";
    }

    private Color getTemperatureColor(double temperatureFactor, Color hotColor, Color coolColor) {
        double t = Math.pow(temperatureFactor, 0.7);

        double r = coolColor.getRed() + (hotColor.getRed() - coolColor.getRed()) * t;
        double g = coolColor.getGreen() + (hotColor.getGreen() - coolColor.getGreen()) * t;
        double b = coolColor.getBlue() + (hotColor.getBlue() - coolColor.getBlue()) * t;

        double brightness = 0.7 + 0.3 * t;
        r = Math.min(1.0, r * brightness);
        g = Math.min(1.0, g * brightness);
        b = Math.min(1.0, b * brightness);

        return Color.color(r, g, b);
    }
}
