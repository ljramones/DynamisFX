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
 * Generates a bubbles effect with particles spawning near the bottom,
 * rising upward with moderate wobble and slight outward expansion,
 * using highly transparent light blue coloring.
 */
public class BubblesGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>();
        double lifetimeRange = config.maxLifetime() - config.minLifetime();
        double sizeRange = config.maxSize() - config.minSize();

        for (int i = 0; i < config.numElements(); i++) {
            double angle = random.nextDouble() * 2.0 * Math.PI;

            // Spawn from center to half the outer radius
            double radius = random.nextDouble() * config.outerRadius() * 0.5;

            // Moderate wobble
            double angularSpeed = config.vortexAngularSpeed() * (0.3 + random.nextDouble() * 0.7);

            // Slight outward expansion
            double radialSpeed = random.nextDouble() * 0.3;

            // Rising motion
            double verticalSpeed = config.vortexVerticalSpeed() + 1.0 + random.nextDouble() * 2.0;

            // Spawn near bottom
            double y = -config.vortexHeight() / 2.0 + random.nextDouble() * config.vortexHeight() * 0.2;

            // Reduced drag for buoyant feel
            double drag = config.drag() * 0.5;

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;

            // Small to medium bubble sizes
            double size = config.minSize() + random.nextDouble() * sizeRange * 0.6;

            // Light blue, very transparent
            double opacity = 0.2 + random.nextDouble() * 0.2;
            Color color = interpolateColor(Color.LIGHTBLUE, Color.LIGHTSKYBLUE, random.nextDouble(), opacity);

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
        return ParticleFieldType.BUBBLES;
    }

    @Override
    public String getDescription() {
        return "Underwater bubbles rising gently from the bottom with slight wobble, " +
                "using transparent light blue tones for a fluid, buoyant feel.";
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
