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
 * Generator for rain particles.
 * <p>
 * Particles spawn at the top plane (emitter width/depth), fall with gravity
 * plus slight random wind sway. Short lifetime. Blue/gray colors.
 */
public class RainGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double[] emitter = config.emitterSize();
        double halfW = emitter[0] / 2.0;
        double halfD = emitter[2] / 2.0;
        double spawnHeight = emitter[1] > 0 ? emitter[1] : config.outerRadius();

        double[] gravity = config.gravity();
        double[] wind = config.wind();
        double lifetimeRange = config.maxLifetime() - config.minLifetime();

        for (int i = 0; i < config.numElements(); i++) {
            // Spawn at top plane with random XZ within emitter
            double x = (random.nextDouble() - 0.5) * 2 * halfW;
            double y = random.nextDouble() * spawnHeight; // stagger start heights
            double z = (random.nextDouble() - 0.5) * 2 * halfD;

            // Slight initial downward velocity with wind sway
            double speed = config.minSpeed() + random.nextDouble() * (config.maxSpeed() - config.minSpeed());
            double vx = wind[0] + (random.nextDouble() - 0.5) * 0.5;
            double vy = -speed; // falling
            double vz = wind[2] + (random.nextDouble() - 0.5) * 0.5;

            double ax = wind[0] * 0.1;
            double ay = gravity[1];
            double az = wind[2] * 0.1;

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;
            double size = config.minSize() + random.nextDouble() * sizeRange;

            // Blue/gray color variation
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());
            // Rain drops are semi-transparent
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(),
                    0.4 + random.nextDouble() * 0.4);

            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    config.drag(), lifetime, size, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.RAIN;
    }

    @Override
    public String getDescription() {
        return "Rain generator - falling particles with gravity and wind sway";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
