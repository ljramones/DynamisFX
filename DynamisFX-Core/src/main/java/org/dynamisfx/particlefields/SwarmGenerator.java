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
package org.dynamisfx.particlefields;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for swarm particles.
 * <p>
 * Erratic grouped particles with randomized velocity changes.
 * Moderate lifetime. Configurable colors.
 * Particles cluster around a center with occasional darting movements.
 */
public class SwarmGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double clusterRadius = config.outerRadius() * 0.3;
        double lifetimeRange = config.maxLifetime() - config.minLifetime();
        double speedRange = config.maxSpeed() - config.minSpeed();

        for (int i = 0; i < config.numElements(); i++) {
            // Cluster around center with Gaussian distribution
            double x = random.nextGaussian() * clusterRadius;
            double y = random.nextGaussian() * clusterRadius;
            double z = random.nextGaussian() * clusterRadius;

            // Erratic velocity - random direction with moderate speed
            double speed = config.minSpeed() + random.nextDouble() * speedRange;
            double theta = random.nextDouble() * 2 * Math.PI;
            double phi = Math.acos(2 * random.nextDouble() - 1);
            double vx = speed * Math.sin(phi) * Math.cos(theta);
            double vy = speed * Math.sin(phi) * Math.sin(theta);
            double vz = speed * Math.cos(phi);

            // Slight centering acceleration (swarm cohesion)
            double dist = Math.sqrt(x * x + y * y + z * z);
            double cohesion = 0.5;
            double ax = dist > 0 ? -x / dist * cohesion : 0;
            double ay = dist > 0 ? -y / dist * cohesion : 0;
            double az = dist > 0 ? -z / dist * cohesion : 0;

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;
            double size = config.minSize() + random.nextDouble() * sizeRange;

            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());
            // Slight glow/transparency
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(),
                    0.5 + random.nextDouble() * 0.5);

            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    config.drag(), lifetime, size, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.SWARM;
    }

    @Override
    public String getDescription() {
        return "Swarm generator - erratic clustered particles with cohesion";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
