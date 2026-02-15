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
package org.dynamisfx.particlefields.noise;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes repulsion forces for particles near obstacles, causing fog/smoke
 * to flow around objects rather than through them.
 *
 * <p>Each obstacle is a sphere with configurable repulsion strength.
 * The repulsion has two components:
 * <ul>
 *   <li><b>Radial</b>: pushes particles away from the obstacle center
 *       with inverse-distance falloff</li>
 *   <li><b>Tangential</b>: swirls particles around the obstacle using
 *       the cross product of the repulsion direction with the curl velocity</li>
 * </ul>
 */
public class ObstacleRepulsion {

    /**
     * Defines a spherical obstacle.
     *
     * @param centerX obstacle center X
     * @param centerY obstacle center Y
     * @param centerZ obstacle center Z
     * @param radius obstacle radius
     * @param repulsionStrength strength of the repulsion force
     */
    public record Obstacle(double centerX, double centerY, double centerZ,
                           double radius, double repulsionStrength) {
    }

    private final List<Obstacle> obstacles = new ArrayList<>();

    public void addObstacle(Obstacle obstacle) {
        obstacles.add(obstacle);
    }

    public void removeObstacle(Obstacle obstacle) {
        obstacles.remove(obstacle);
    }

    public void clearObstacles() {
        obstacles.clear();
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    /**
     * Calculates the total repulsion displacement for a particle at (x, y, z)
     * given its current curl displacement (curlDx, curlDy, curlDz).
     *
     * @return array of [dx, dy, dz] displacement to add
     */
    public double[] calculateRepulsion(double x, double y, double z,
                                        double curlDx, double curlDy, double curlDz) {
        double totalDx = 0, totalDy = 0, totalDz = 0;

        for (Obstacle obs : obstacles) {
            double dx = x - obs.centerX();
            double dy = y - obs.centerY();
            double dz = z - obs.centerZ();
            double distSq = dx * dx + dy * dy + dz * dz;
            double dist = Math.sqrt(distSq);

            if (dist < 0.001) continue;

            double effectiveRadius = obs.radius() * 2.0; // repulsion zone extends beyond surface
            if (dist > effectiveRadius) continue;

            // Inverse-distance falloff: stronger closer to obstacle
            double falloff = 1.0 - (dist / effectiveRadius);
            falloff = falloff * falloff; // quadratic falloff for smoother edges
            double strength = obs.repulsionStrength() * falloff;

            // Radial repulsion (push away from center)
            double invDist = 1.0 / dist;
            double nx = dx * invDist;
            double ny = dy * invDist;
            double nz = dz * invDist;
            totalDx += nx * strength;
            totalDy += ny * strength;
            totalDz += nz * strength;

            // Tangential swirl: cross product of repulsion normal with curl direction
            double curlMag = Math.sqrt(curlDx * curlDx + curlDy * curlDy + curlDz * curlDz);
            if (curlMag > 0.001) {
                double swirlStrength = strength * 0.5;
                // cross(normal, curlDir)
                double cx = ny * curlDz - nz * curlDy;
                double cy = nz * curlDx - nx * curlDz;
                double cz = nx * curlDy - ny * curlDx;
                double cMag = Math.sqrt(cx * cx + cy * cy + cz * cz);
                if (cMag > 0.001) {
                    double invCMag = swirlStrength / cMag;
                    totalDx += cx * invCMag;
                    totalDy += cy * invCMag;
                    totalDz += cz * invCMag;
                }
            }
        }

        return new double[]{totalDx, totalDy, totalDz};
    }
}
