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
 * Generator for lightning particles.
 * <p>
 * Particles are placed along a fractal branching path from top to bottom.
 * Starts at (0, spawnHeight, 0) and random-walks downward with branching
 * every ~5 particles. Near-zero velocity, no acceleration, no drag,
 * white to blue-white color, full opacity, very short lifetime.
 */
public class LightningGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double[] emitter = config.emitterSize();
        double spawnHeight = emitter[1] > 0 ? emitter[1] : config.outerRadius();
        double sizeRange = config.maxSize() - config.minSize();
        double lifetimeRange = config.maxLifetime() - config.minLifetime();

        // Step size for walking down
        double stepY = spawnHeight / Math.max(1, config.numElements());

        // Current position along the main bolt
        double cx = 0;
        double cy = spawnHeight;
        double cz = 0;

        // Branch tracking
        double bx = 0, by = 0, bz = 0;
        boolean inBranch = false;
        int branchRemaining = 0;

        for (int i = 0; i < config.numElements(); i++) {
            double px, py, pz;

            if (inBranch && branchRemaining > 0) {
                // Continue branch with wider offsets
                bx += (random.nextDouble() - 0.5) * 4.0;
                by -= stepY * (0.5 + random.nextDouble() * 0.5);
                bz += (random.nextDouble() - 0.5) * 4.0;
                px = bx;
                py = by;
                pz = bz;
                branchRemaining--;
                if (branchRemaining <= 0) {
                    inBranch = false;
                }
            } else {
                // Main bolt: walk downward with random XZ offsets
                cx += (random.nextDouble() - 0.5) * 2.0;
                cy -= stepY;
                cz += (random.nextDouble() - 0.5) * 2.0;
                px = cx;
                py = cy;
                pz = cz;

                // Every ~5 particles, start a branch
                if (i > 0 && i % 5 == 0 && i < config.numElements() - 3) {
                    inBranch = true;
                    branchRemaining = 2 + random.nextInt(3);
                    bx = cx;
                    by = cy;
                    bz = cz;
                }
            }

            // Near-zero velocity (slight random jitter)
            double vx = (random.nextDouble() - 0.5) * 0.05;
            double vy = (random.nextDouble() - 0.5) * 0.05;
            double vz = (random.nextDouble() - 0.5) * 0.05;

            // No acceleration, no drag
            double ax = 0;
            double ay = 0;
            double az = 0;

            // Very short lifetime
            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;
            // Small particle size
            double size = config.minSize() + random.nextDouble() * sizeRange * 0.5;

            // White to blue-white color, full opacity
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(), 1.0);

            elements.add(new ParticleFieldElement(
                    px, py, pz, vx, vy, vz, ax, ay, az,
                    0, lifetime, size, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.LIGHTNING;
    }

    @Override
    public String getDescription() {
        return "Lightning generator - fractal branching path with very short lifetime";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
