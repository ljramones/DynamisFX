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
package org.dynamisfx.particlefields.vortex;

import javafx.scene.paint.Color;
import org.dynamisfx.particlefields.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates a magical aura effect with particles spawning near the center,
 * gently expanding outward and rising upward with slow angular rotation
 * and green-to-gold coloring.
 */
public class MagicAuraGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>();
        double lifetimeRange = config.maxLifetime() - config.minLifetime();
        double sizeRange = config.maxSize() - config.minSize();
        double radiusRange = config.outerRadius() - config.innerRadius();

        for (int i = 0; i < config.numElements(); i++) {
            double angle = random.nextDouble() * 2.0 * Math.PI;

            // Biased near center: only use 30% of the radius range
            double radius = config.innerRadius() + random.nextDouble() * radiusRange * 0.3;

            // Slow rotation
            double angularSpeed = config.vortexAngularSpeed() * 0.5
                    * (0.5 + random.nextDouble() * config.vortexTightness());

            // Slight outward expansion
            double radialSpeed = 0.2 + random.nextDouble() * 0.5;

            // Gentle upward movement
            double verticalSpeed = 0.5 + random.nextDouble() * 1.0;

            // Spawn near bottom of vortex height
            double y = -config.vortexHeight() / 2.0 + random.nextDouble() * config.vortexHeight() * 0.3;

            double drag = config.drag();

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;

            double size = config.minSize() + random.nextDouble() * sizeRange;

            double t = random.nextDouble();
            double opacity = 0.4 + random.nextDouble() * 0.3;
            Color color = interpolateColor(Color.GREEN, Color.GOLD, t, opacity);

            elements.add(ParticleFieldElement.createVortex(
                    angle, radius,
                    angularSpeed, radialSpeed, verticalSpeed,
                    y, drag,
                    lifetime,
                    size,
                    color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.MAGIC_AURA;
    }

    @Override
    public String getDescription() {
        return "Mystical aura with particles rising gently from the center, " +
                "slowly rotating outward with an enchanted green-to-gold glow.";
    }

    private Color interpolateColor(Color from, Color to, double t, double opacity) {
        double r = from.getRed() + (to.getRed() - from.getRed()) * t;
        double g = from.getGreen() + (to.getGreen() - from.getGreen()) * t;
        double b = from.getBlue() + (to.getBlue() - from.getBlue()) * t;
        return new Color(
                Math.max(0, Math.min(1, r)),
                Math.max(0, Math.min(1, g)),
                Math.max(0, Math.min(1, b)),
                Math.max(0, Math.min(1, opacity))
        );
    }
}
