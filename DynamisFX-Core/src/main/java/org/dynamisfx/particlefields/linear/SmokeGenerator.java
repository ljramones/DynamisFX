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
 * Generator for smoke particles.
 * <p>
 * Particles spawn at the base (like fire but with wider spread), rise slowly
 * with wide horizontal spread, mild buoyancy, horizontal turbulence, and
 * higher drag than fire. Gray colors with decreasing opacity.
 */
public class SmokeGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double[] emitter = config.emitterSize();
        double halfW = emitter[0] / 2.0;
        double halfD = emitter[2] / 2.0;
        double lifetimeRange = config.maxLifetime() - config.minLifetime();
        double speedRange = config.maxSpeed() - config.minSpeed();

        for (int i = 0; i < config.numElements(); i++) {
            // Spawn at base with wider spread than fire
            double x = (random.nextDouble() - 0.5) * 2 * halfW;
            double y = (random.nextDouble() - 0.5) * emitter[1] * 0.2;
            double z = (random.nextDouble() - 0.5) * 2 * halfD;

            // Slow upward velocity with wide horizontal spread
            double speed = config.minSpeed() + random.nextDouble() * speedRange;
            double vx = (random.nextDouble() - 0.5) * speed * 0.6;
            double vy = speed * 0.5; // slow rise
            double vz = (random.nextDouble() - 0.5) * speed * 0.6;

            // Mild buoyancy upward, horizontal turbulence
            double ax = (random.nextDouble() - 0.5) * 1.0;
            double ay = 1.5 + random.nextDouble() * 0.5;
            double az = (random.nextDouble() - 0.5) * 1.0;

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;
            double size = config.minSize() + random.nextDouble() * sizeRange;

            // Gray colors (dark gray to light gray), decreasing opacity
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(),
                    0.3 + random.nextDouble() * 0.4);

            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    config.drag(), lifetime, size, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.SMOKE;
    }

    @Override
    public String getDescription() {
        return "Smoke generator - slow rising particles with wide spread and drag";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
