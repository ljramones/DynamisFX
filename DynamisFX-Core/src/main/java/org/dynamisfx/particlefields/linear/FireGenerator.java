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
 * Generator for fire particles.
 * <p>
 * Particles spawn at base point/area, rise upward with turbulence.
 * Short lifetime. Temperature-based color gradient (yellow to orange to red to dark).
 * Upward acceleration opposes gravity.
 */
public class FireGenerator implements ParticleFieldGenerator {

    @Override
    public List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random) {
        List<ParticleFieldElement> elements = new ArrayList<>(config.numElements());

        double sizeRange = config.maxSize() - config.minSize();
        double[] emitter = config.emitterSize();
        double halfW = emitter[0] / 2.0;
        double halfD = emitter[2] / 2.0;
        double lifetimeRange = config.maxLifetime() - config.minLifetime();
        double speedRange = config.maxSpeed() - config.minSpeed();
        double spreadRad = Math.toRadians(config.spreadAngle());

        for (int i = 0; i < config.numElements(); i++) {
            // Spawn at base with slight random spread
            double x = (random.nextDouble() - 0.5) * 2 * halfW;
            double y = (random.nextDouble() - 0.5) * emitter[1] * 0.2; // near base
            double z = (random.nextDouble() - 0.5) * 2 * halfD;

            // Upward velocity with turbulent spread
            double speed = config.minSpeed() + random.nextDouble() * speedRange;
            double angleX = (random.nextDouble() - 0.5) * 2 * spreadRad;
            double angleZ = (random.nextDouble() - 0.5) * 2 * spreadRad;
            double vx = speed * Math.sin(angleX) + (random.nextDouble() - 0.5) * 1.0;
            double vy = speed * Math.cos(angleX) * Math.cos(angleZ); // upward
            double vz = speed * Math.sin(angleZ) + (random.nextDouble() - 0.5) * 1.0;

            // Upward buoyancy acceleration (opposes gravity)
            double ax = (random.nextDouble() - 0.5) * 2.0; // turbulence
            double ay = 3.0 + random.nextDouble() * 2.0;    // buoyancy
            double az = (random.nextDouble() - 0.5) * 2.0;  // turbulence

            double lifetime = config.minLifetime() + random.nextDouble() * lifetimeRange;
            double size = config.minSize() + random.nextDouble() * sizeRange;

            // Temperature-based color: young = yellow/white, old = orange/red/dark
            // Start with bright color (age-based fading handled by renderer or via color)
            double tempFactor = random.nextDouble();
            Color color = getFireColor(tempFactor, config.primaryColor(), config.secondaryColor());
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(),
                    0.6 + random.nextDouble() * 0.4);

            elements.add(new ParticleFieldElement(
                    x, y, z, vx, vy, vz, ax, ay, az,
                    config.drag(), lifetime, size, color
            ));
        }

        return elements;
    }

    @Override
    public ParticleFieldType getFieldType() {
        return ParticleFieldType.FIRE;
    }

    @Override
    public String getDescription() {
        return "Fire generator - rising particles with turbulence and temperature colors";
    }

    private Color getFireColor(double t, Color hotColor, Color coolColor) {
        // t=0 -> hot (yellow/white), t=1 -> cool (red/dark)
        double r = hotColor.getRed() + (coolColor.getRed() - hotColor.getRed()) * t;
        double g = hotColor.getGreen() + (coolColor.getGreen() - hotColor.getGreen()) * t;
        double b = hotColor.getBlue() + (coolColor.getBlue() - hotColor.getBlue()) * t;
        double brightness = 1.0 - t * 0.5;
        return Color.color(
                Math.min(1.0, r * brightness),
                Math.min(1.0, g * brightness),
                Math.min(1.0, b * brightness)
        );
    }
}
