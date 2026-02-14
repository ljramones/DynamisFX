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
 * Generator for cloud particles.
 * <p>
 * Particles spawn across a wide emitter volume with very slow drift,
 * nearly zero acceleration, zero drag, and very long lifetime.
 * White to light gray, moderate opacity, large particle sizes.
 */
public class CloudsGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double[] emitter = config.emitterSize();
        double halfW = emitter[0] / 2.0;
        double halfH = emitter[1] / 2.0;
        double halfD = emitter[2] / 2.0;

        for (int i = 0; i < config.numElements(); i++) {
            // Spawn spread across wide emitter volume
            double x = (random.nextDouble() - 0.5) * 2 * halfW;
            double y = halfH + (random.nextDouble() - 0.5) * halfH * 0.5;
            double z = (random.nextDouble() - 0.5) * 2 * halfD;

            // Very slow drift
            double vx = (random.nextDouble() - 0.5) * 0.1;
            double vy = (random.nextDouble() - 0.5) * 0.02;
            double vz = (random.nextDouble() - 0.5) * 0.1;

            // Nearly zero acceleration
            double ax = (random.nextDouble() - 0.5) * 0.005;
            double ay = 0;
            double az = (random.nextDouble() - 0.5) * 0.005;

            // Very long lifetime (use config values, or -1 if maxLifetime < 0)
            double lifetime = config.maxLifetime() < 0
                    ? -1
                    : config.minLifetime() + random.nextDouble() * (config.maxLifetime() - config.minLifetime());

            // Large size, biased toward maxSize * 1.5
            double size = config.maxSize() + random.nextDouble() * sizeRange * 0.5;

            // White to light gray, moderate opacity
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(),
                    0.3 + random.nextDouble() * 0.2);

            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    0, lifetime, size, color // zero drag
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.CLOUDS;
    }

    @Override
    public String getDescription() {
        return "Clouds generator - large slow-drifting particles with long lifetime";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
