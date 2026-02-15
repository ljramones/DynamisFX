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
 * Generator for fog particles.
 * <p>
 * Particles spawn within the full emitter volume biased to the bottom half.
 * Near-zero gravity, slow horizontal drift, high opacity, gray/white colors,
 * and large particle sizes.
 */
public class FogGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double[] emitter = config.emitterSize();
        double halfW = emitter[0] / 2.0;
        double halfH = emitter[1] / 2.0;
        double halfD = emitter[2] / 2.0;

        double[] gravity = config.gravity();
        double lifetimeRange = config.maxLifetime() - config.minLifetime();

        for (int i = 0; i < config.numElements(); i++) {
            // Spawn within emitter volume, biased to bottom half
            double x = (random.nextDouble() - 0.5) * 2 * halfW;
            double y = random.nextDouble() * random.nextDouble() * halfH; // biased low
            double z = (random.nextDouble() - 0.5) * 2 * halfD;

            // Very slow horizontal drift, near-zero vertical
            double vx = (random.nextDouble() - 0.5) * 0.3;
            double vy = (random.nextDouble() - 0.5) * 0.05;
            double vz = (random.nextDouble() - 0.5) * 0.3;

            // Near-zero acceleration
            double ax = (random.nextDouble() - 0.5) * 0.02;
            double ay = gravity[1] * 0.05;
            double az = (random.nextDouble() - 0.5) * 0.02;

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;
            // Large particle sizes, biased toward maxSize
            double size = config.minSize() + (0.5 + random.nextDouble() * 0.5) * sizeRange;

            // Gray/white colors with opacity centered on configured alpha.
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());
            double baseOpacity = Math.max(config.primaryColor().getOpacity(), config.secondaryColor().getOpacity());
            double minOpacity = Math.max(0.002, baseOpacity * 0.7);
            double maxOpacity = Math.min(0.2, baseOpacity * 1.3 + 0.002);
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(),
                    minOpacity + random.nextDouble() * Math.max(0.001, maxOpacity - minOpacity));

            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    config.drag(), lifetime, size, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.FOG;
    }

    @Override
    public String getDescription() {
        return "Fog generator - slow drifting particles with configurable low opacity near ground level";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
