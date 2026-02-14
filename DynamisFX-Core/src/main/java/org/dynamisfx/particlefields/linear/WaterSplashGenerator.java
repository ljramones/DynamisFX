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
package org.dynamisfx.particlefields.linear;

import javafx.scene.paint.Color;
import org.dynamisfx.particlefields.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for water splash particles.
 * <p>
 * Particles spawn at center point (0, 0, 0) and burst outward in a
 * hemispherical pattern with high initial speed. Strong gravity pulls
 * them back down. Blue to white colors, semi-transparent, short lifetime.
 */
public class WaterSplashGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double[] gravity = config.gravity();
        double lifetimeRange = config.maxLifetime() - config.minLifetime();
        double speedRange = config.maxSpeed() - config.minSpeed();

        for (int i = 0; i < config.numElements(); i++) {
            // Spawn at center point
            double x = 0;
            double y = 0;
            double z = 0;

            // Hemispherical burst: random direction in upper hemisphere
            double speed = config.minSpeed() + random.nextDouble() * speedRange;
            double theta = random.nextDouble() * Math.PI * 2; // azimuth
            double phi = random.nextDouble() * Math.PI * 0.5; // elevation (upper hemisphere)
            double vx = speed * Math.sin(phi) * Math.cos(theta);
            double vy = speed * Math.cos(phi); // upward
            double vz = speed * Math.sin(phi) * Math.sin(theta);

            // Strong gravity pulling down (full gravity)
            double ax = 0;
            double ay = gravity[1];
            double az = 0;

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;
            double size = config.minSize() + random.nextDouble() * sizeRange;

            // Blue to white colors, semi-transparent
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(),
                    0.5 + random.nextDouble() * 0.3);

            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    config.drag(), lifetime, size, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.WATER_SPLASH;
    }

    @Override
    public String getDescription() {
        return "Water splash generator - hemispherical burst with strong gravity pullback";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
