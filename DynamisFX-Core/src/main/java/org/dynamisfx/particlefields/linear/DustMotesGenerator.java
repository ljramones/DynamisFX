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
 * Generator for dust mote particles.
 * <p>
 * Particles spawn throughout the emitter volume with very slow random
 * Brownian-like velocity in all directions. Small random acceleration changes,
 * low drag, golden/amber colors, semi-transparent, very small size,
 * and long lifetime.
 */
public class DustMotesGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double[] emitter = config.emitterSize();
        double halfW = emitter[0] / 2.0;
        double halfH = emitter[1] / 2.0;
        double halfD = emitter[2] / 2.0;

        double lifetimeRange = config.maxLifetime() - config.minLifetime();

        for (int i = 0; i < config.numElements(); i++) {
            // Spawn throughout volume
            double x = (random.nextDouble() - 0.5) * 2 * halfW;
            double y = (random.nextDouble() - 0.5) * 2 * halfH;
            double z = (random.nextDouble() - 0.5) * 2 * halfD;

            // Very slow random velocity in all directions (Brownian)
            double vx = (random.nextDouble() - 0.5) * 0.1;
            double vy = (random.nextDouble() - 0.5) * 0.1;
            double vz = (random.nextDouble() - 0.5) * 0.1;

            // Small random acceleration changes (Brownian motion)
            double ax = (random.nextDouble() - 0.5) * 0.05;
            double ay = (random.nextDouble() - 0.5) * 0.05;
            double az = (random.nextDouble() - 0.5) * 0.05;

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;
            // Very small size
            double size = config.minSize() + random.nextDouble() * sizeRange * 0.3;

            // Golden/amber colors, semi-transparent
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(),
                    0.3 + random.nextDouble() * 0.3);

            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    config.drag(), lifetime, size, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.DUST_MOTES;
    }

    @Override
    public String getDescription() {
        return "Dust motes generator - tiny floating particles with Brownian-like motion";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
