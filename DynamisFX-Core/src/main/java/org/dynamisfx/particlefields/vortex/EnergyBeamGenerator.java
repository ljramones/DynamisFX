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
 * Generates a tight, high-energy beam effect with particles confined to a narrow
 * radius, spinning rapidly and shooting upward at high speed with bright
 * white-to-cyan coloring.
 */
public class EnergyBeamGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>();
        double lifetimeRange = config.maxLifetime() - config.minLifetime();
        double sizeRange = config.maxSize() - config.minSize();

        for (int i = 0; i < config.numElements(); i++) {
            double angle = random.nextDouble() * 2.0 * Math.PI;

            // Very tight beam radius
            double radius = random.nextDouble() * config.outerRadius() * 0.15;

            // Very fast angular speed
            double angularSpeed = config.vortexAngularSpeed() * 3.0
                    * (0.8 + random.nextDouble() * config.vortexTightness());

            // Near-zero radial speed to stay tight
            double radialSpeed = random.nextDouble() * 0.1 - 0.05;

            // Very high vertical speed
            double verticalSpeed = config.vortexVerticalSpeed() + 5.0 + random.nextDouble() * 5.0;

            // Start at bottom
            double y = -config.vortexHeight() / 2.0;

            // Very low drag for sustained velocity
            double drag = config.drag() * 0.2;

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;

            // Small particles
            double size = config.minSize() + random.nextDouble() * sizeRange * 0.4;

            double t = random.nextDouble();
            double opacity = 0.8 + random.nextDouble() * 0.2;
            Color color = interpolateColor(Color.WHITE, Color.CYAN, t, opacity);

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
        return ParticleFieldType.ENERGY_BEAM;
    }

    @Override
    public String getDescription() {
        return "High-energy beam with tightly focused particles spinning rapidly " +
                "and shooting upward at extreme speed, glowing bright white to cyan.";
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
