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
 * Generator for explosion particles.
 * <p>
 * Radial burst from center point. High initial speed, strong drag for deceleration.
 * Short lifetime. Orange/yellow sparks fading to gray.
 */
public class ExplosionGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double lifetimeRange = config.maxLifetime() - config.minLifetime();
        double speedRange = config.maxSpeed() - config.minSpeed();

        for (int i = 0; i < config.numElements(); i++) {
            // All start at or near center
            double x = (random.nextGaussian()) * 0.5;
            double y = (random.nextGaussian()) * 0.5;
            double z = (random.nextGaussian()) * 0.5;

            // Radial velocity - random direction, high speed
            double speed = config.minSpeed() + random.nextDouble() * speedRange;
            // Uniform random direction on sphere
            double theta = random.nextDouble() * 2 * Math.PI;
            double phi = Math.acos(2 * random.nextDouble() - 1);
            double vx = speed * Math.sin(phi) * Math.cos(theta);
            double vy = speed * Math.sin(phi) * Math.sin(theta);
            double vz = speed * Math.cos(phi);

            // Gravity pulls down slightly
            double[] gravity = config.gravity();
            double ax = gravity[0];
            double ay = gravity[1] * 0.3; // reduced gravity for explosions
            double az = gravity[2];

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;
            double size = config.minSize() + random.nextDouble() * sizeRange;

            // Orange/yellow sparks
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(),
                    0.7 + random.nextDouble() * 0.3);

            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    config.drag(), lifetime, size, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.EXPLOSION;
    }

    @Override
    public String getDescription() {
        return "Explosion generator - radial burst with high speed and strong drag";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
