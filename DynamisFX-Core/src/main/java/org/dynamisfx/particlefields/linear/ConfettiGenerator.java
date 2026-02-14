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
 * Generator for confetti particles.
 * <p>
 * Particles spawn at the top of the emitter spread and flutter down with
 * random lateral movement. Multi-color bright hues (red, blue, green, yellow,
 * pink, purple), full opacity, moderate gravity and drag.
 */
public class ConfettiGenerator implements ParticleFieldGenerator {

    private static final Color[] CONFETTI_COLORS = {
            Color.RED,
            Color.BLUE,
            Color.LIMEGREEN,
            Color.YELLOW,
            Color.HOTPINK,
            Color.PURPLE
    };

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double[] emitter = config.emitterSize();
        double halfW = emitter[0] / 2.0;
        double halfD = emitter[2] / 2.0;
        double spawnHeight = emitter[1] > 0 ? emitter[1] : config.outerRadius();

        double[] gravity = config.gravity();
        double lifetimeRange = config.maxLifetime() - config.minLifetime();

        for (int i = 0; i < config.numElements(); i++) {
            // Spawn at top of emitter spread
            double x = (random.nextDouble() - 0.5) * 2 * halfW;
            double y = random.nextDouble() * spawnHeight;
            double z = (random.nextDouble() - 0.5) * 2 * halfD;

            // Slow downward velocity with random lateral flutter
            double speed = config.minSpeed() + random.nextDouble() * (config.maxSpeed() - config.minSpeed());
            double vx = (random.nextDouble() - 0.5) * speed * 0.8;
            double vy = -speed * 0.5;
            double vz = (random.nextDouble() - 0.5) * speed * 0.8;

            // Moderate gravity, random lateral flutter acceleration
            double ax = (random.nextDouble() - 0.5) * 1.5;
            double ay = gravity[1] * 0.5;
            double az = (random.nextDouble() - 0.5) * 1.5;

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;
            // Small to moderate size
            double size = config.minSize() + random.nextDouble() * sizeRange * 0.7;

            // Multi-color: randomly pick from bright confetti colors
            Color color = CONFETTI_COLORS[random.nextInt(CONFETTI_COLORS.length)];
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(), 1.0);

            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    config.drag(), lifetime, size, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.CONFETTI;
    }

    @Override
    public String getDescription() {
        return "Confetti generator - colorful fluttering particles falling from above";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
