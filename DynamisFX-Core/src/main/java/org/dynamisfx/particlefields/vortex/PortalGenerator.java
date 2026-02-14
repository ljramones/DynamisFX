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
 * Generates a portal vortex effect where particles spawn at the outer radius
 * and spiral inward with fast angular speed, using blue-to-purple coloring.
 */
public class PortalGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>();
        double lifetimeRange = config.maxLifetime() - config.minLifetime();
        double sizeRange = config.maxSize() - config.minSize();
        double radiusRange = config.outerRadius() - config.innerRadius();

        for (int i = 0; i < config.numElements(); i++) {
            double angle = random.nextDouble() * 2.0 * Math.PI;

            // Bias toward outer radius
            double radiusFactor = random.nextDouble();
            radiusFactor = radiusFactor * radiusFactor; // square to bias outer
            double radius = config.outerRadius() - radiusFactor * radiusRange;

            double angularSpeed = config.vortexAngularSpeed() * (1.0 + random.nextDouble() * config.vortexTightness());

            // Negative radial speed to spiral inward, at least -1.0
            double radialSpeed = -(config.vortexRadialSpeed() + random.nextDouble() * 2.0);
            if (radialSpeed > -1.0) {
                radialSpeed = -1.0;
            }

            double verticalSpeed = config.vortexVerticalSpeed() + (random.nextDouble() - 0.5) * 1.0;

            double y = (random.nextDouble() - 0.5) * config.vortexHeight();

            double drag = config.drag();

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;

            double size = config.minSize() + random.nextDouble() * sizeRange;

            double t = random.nextDouble();
            double opacity = 0.5 + random.nextDouble() * 0.4;
            Color color = interpolateColor(Color.BLUE, Color.PURPLE, t, opacity);

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
        return ParticleFieldType.PORTAL;
    }

    @Override
    public String getDescription() {
        return "Portal vortex with particles spiraling inward from the outer edge, " +
                "creating a swirling blue-to-purple dimensional gateway effect.";
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
