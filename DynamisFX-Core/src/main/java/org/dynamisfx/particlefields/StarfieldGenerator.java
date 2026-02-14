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
 * Generator for starfield particles.
 * <p>
 * Static or very slowly drifting particles in spherical volume.
 * Infinite lifetime. White/blue-white colors with random brightness.
 * Twinkle effect via opacity variation.
 */
public class StarfieldGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double radius = config.outerRadius();

        for (int i = 0; i < config.numElements(); i++) {
            // Uniform distribution in spherical volume
            double theta = random.nextDouble() * 2 * Math.PI;
            double phi = Math.acos(2 * random.nextDouble() - 1);
            double r = radius * Math.cbrt(random.nextDouble()); // cube root for uniform volume

            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = r * Math.sin(phi) * Math.sin(theta);
            double z = r * Math.cos(phi);

            // Very slow drift or static
            double speed = config.minSpeed() + random.nextDouble() * (config.maxSpeed() - config.minSpeed());
            double vx = (random.nextDouble() - 0.5) * speed * 0.1;
            double vy = (random.nextDouble() - 0.5) * speed * 0.1;
            double vz = (random.nextDouble() - 0.5) * speed * 0.1;

            // No acceleration, no drag for stars
            double ax = 0, ay = 0, az = 0;

            double size = config.minSize() + random.nextDouble() * sizeRange;

            // White/blue-white with random brightness for twinkle
            Color baseColor = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());
            double brightness = 0.3 + random.nextDouble() * 0.7;
            Color color = Color.color(
                    Math.min(1.0, baseColor.getRed() * brightness),
                    Math.min(1.0, baseColor.getGreen() * brightness),
                    Math.min(1.0, baseColor.getBlue() * brightness),
                    0.5 + random.nextDouble() * 0.5 // twinkle via opacity
            );

            // Infinite lifetime
            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    0.0, -1, size, color // lifetime = -1 means infinite
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.STARFIELD;
    }

    @Override
    public String getDescription() {
        return "Starfield generator - static/drifting particles with twinkle effect";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
