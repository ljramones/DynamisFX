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

/**
 * Represents a single particle/element in a particle field.
 * Supports two motion models:
 * <ul>
 *   <li><b>ORBITAL</b> - Keplerian mechanics with orbital parameters</li>
 *   <li><b>LINEAR</b> - Velocity/acceleration with finite lifetime</li>
 * </ul>
 * The renderer reads (x, y, z, color, size, opacity) regardless of motion model.
 */
public class ParticleFieldElement {

    // Motion model
    private final MotionModel motionModel;

    // === Shared fields ===
    private final double size;
    private final Color color;
    private double x, y, z;

    // === ORBITAL fields ===
    private final double semiMajorAxis;
    private final double eccentricity;
    private final double inclination;
    private final double argumentOfPeriapsis;
    private final double longitudeOfAscendingNode;
    private final double angularSpeed;
    private final double heightOffset;
    private double currentAngle;

    // === LINEAR fields ===
    private double vx, vy, vz;
    private double ax, ay, az;
    private double drag;
    private double lifetime;
    private double age;

    /**
     * Orbital constructor - creates a particle with Keplerian orbital mechanics.
     */
    public ParticleFieldElement(
            double semiMajorAxis,
            double eccentricity,
            double inclination,
            double argumentOfPeriapsis,
            double longitudeOfAscendingNode,
            double initialAngle,
            double angularSpeed,
            double size,
            double heightOffset,
            Color color
    ) {
        this.motionModel = MotionModel.ORBITAL;
        this.semiMajorAxis = semiMajorAxis;
        this.eccentricity = eccentricity;
        this.inclination = inclination;
        this.argumentOfPeriapsis = argumentOfPeriapsis;
        this.longitudeOfAscendingNode = longitudeOfAscendingNode;
        this.currentAngle = initialAngle;
        this.angularSpeed = angularSpeed;
        this.size = size;
        this.heightOffset = heightOffset;
        this.color = color;

        // Linear fields default
        this.lifetime = -1;
        this.age = 0;

        // Initialize position
        updateOrbitalPosition();
    }

    /**
     * Linear constructor - creates a particle with velocity-based motion.
     *
     * @param x        initial x position
     * @param y        initial y position
     * @param z        initial z position
     * @param vx       initial x velocity
     * @param vy       initial y velocity
     * @param vz       initial z velocity
     * @param ax       x acceleration (e.g. gravity)
     * @param ay       y acceleration
     * @param az       z acceleration
     * @param drag     velocity damping factor (0 = none, 1 = full)
     * @param lifetime seconds before expiry (-1 = infinite)
     * @param size     particle size
     * @param color    particle color
     */
    public ParticleFieldElement(
            double x, double y, double z,
            double vx, double vy, double vz,
            double ax, double ay, double az,
            double drag,
            double lifetime,
            double size,
            Color color
    ) {
        this.motionModel = MotionModel.LINEAR;
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.drag = drag;
        this.lifetime = lifetime;
        this.age = 0;
        this.size = size;
        this.color = color;

        // Orbital fields default
        this.semiMajorAxis = 0;
        this.eccentricity = 0;
        this.inclination = 0;
        this.argumentOfPeriapsis = 0;
        this.longitudeOfAscendingNode = 0;
        this.angularSpeed = 0;
        this.heightOffset = 0;
        this.currentAngle = 0;
    }

    /**
     * Advances the element's position by the given time scale.
     * Dispatches to the appropriate motion model.
     *
     * @param timeScale multiplier for movement (1.0 = normal speed)
     */
    public void advance(double timeScale) {
        if (motionModel == MotionModel.ORBITAL) {
            advanceOrbital(timeScale);
        } else {
            advanceLinear(timeScale);
        }
    }

    private void advanceOrbital(double timeScale) {
        currentAngle += angularSpeed * timeScale;
        updateOrbitalPosition();
    }

    private void advanceLinear(double timeScale) {
        // Update velocity with acceleration
        vx += ax * timeScale;
        vy += ay * timeScale;
        vz += az * timeScale;

        // Apply drag
        if (drag > 0) {
            double dampFactor = 1.0 - drag * timeScale;
            if (dampFactor < 0) dampFactor = 0;
            vx *= dampFactor;
            vy *= dampFactor;
            vz *= dampFactor;
        }

        // Update position
        x += vx * timeScale;
        y += vy * timeScale;
        z += vz * timeScale;

        // Update age
        age += timeScale;
    }

    /**
     * Updates the cached x, y, z position based on current orbital angle.
     */
    private void updateOrbitalPosition() {
        // Calculate radius at current true anomaly: r = a(1-e^2)/(1+e*cos(v))
        double r;
        if (eccentricity < 1e-10) {
            r = semiMajorAxis;
        } else {
            r = semiMajorAxis * (1 - eccentricity * eccentricity)
                    / (1 + eccentricity * Math.cos(currentAngle));
        }

        // Position in orbital plane
        double xOrbital = r * Math.cos(currentAngle);
        double yOrbital = r * Math.sin(currentAngle);

        // Apply argument of periapsis rotation (in orbital plane)
        double cosArgPeri = Math.cos(argumentOfPeriapsis);
        double sinArgPeri = Math.sin(argumentOfPeriapsis);
        double xRotated = xOrbital * cosArgPeri - yOrbital * sinArgPeri;
        double yRotated = xOrbital * sinArgPeri + yOrbital * cosArgPeri;

        // Apply inclination (tilt the orbital plane)
        double cosInc = Math.cos(inclination);
        double sinInc = Math.sin(inclination);
        double zTilted = yRotated * sinInc;
        double yTilted = yRotated * cosInc;

        // Apply longitude of ascending node (rotate around z-axis)
        double cosLAN = Math.cos(longitudeOfAscendingNode);
        double sinLAN = Math.sin(longitudeOfAscendingNode);
        this.x = xRotated * cosLAN - yTilted * sinLAN;
        this.z = xRotated * sinLAN + yTilted * cosLAN;
        this.y = zTilted + heightOffset;
    }

    /**
     * Returns whether this particle is still alive.
     * Orbital particles (lifetime = -1) are always alive.
     * Linear particles die when age exceeds lifetime.
     */
    public boolean isAlive() {
        return lifetime < 0 || age < lifetime;
    }

    // === Getters for shared properties ===
    public MotionModel getMotionModel() { return motionModel; }
    public double getSize() { return size; }
    public Color getColor() { return color; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    // === Getters for orbital properties ===
    public double getSemiMajorAxis() { return semiMajorAxis; }
    public double getEccentricity() { return eccentricity; }
    public double getInclination() { return inclination; }
    public double getArgumentOfPeriapsis() { return argumentOfPeriapsis; }
    public double getLongitudeOfAscendingNode() { return longitudeOfAscendingNode; }
    public double getAngularSpeed() { return angularSpeed; }
    public double getHeightOffset() { return heightOffset; }
    public double getCurrentAngle() { return currentAngle; }

    // === Getters for linear properties ===
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getVz() { return vz; }
    public double getAx() { return ax; }
    public double getAy() { return ay; }
    public double getAz() { return az; }
    public double getDrag() { return drag; }
    public double getLifetime() { return lifetime; }
    public double getAge() { return age; }

    /**
     * Sets the position directly (used by physics engines or respawning).
     */
    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
