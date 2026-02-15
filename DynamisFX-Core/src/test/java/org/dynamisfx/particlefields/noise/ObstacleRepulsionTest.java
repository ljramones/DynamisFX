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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ObstacleRepulsion verifying repulsion direction, falloff, and tangential swirl.
 */
public class ObstacleRepulsionTest {

    private ObstacleRepulsion repulsion;

    @BeforeEach
    void setUp() {
        repulsion = new ObstacleRepulsion();
    }

    @Test
    @DisplayName("Particle near obstacle is pushed away radially")
    void radialRepulsion() {
        repulsion.addObstacle(new ObstacleRepulsion.Obstacle(0, 0, 0, 10, 5.0));

        // Particle at (5, 0, 0) - inside the effective radius (2 * 10 = 20)
        double[] result = repulsion.calculateRepulsion(5, 0, 0, 0, 0, 0);

        // Should be pushed in +x direction (away from center)
        assertThat("Repulsion X should be positive (away from center)", result[0], greaterThan(0.0));
        assertThat("Repulsion Y should be ~zero", Math.abs(result[1]), lessThan(0.01));
        assertThat("Repulsion Z should be ~zero", Math.abs(result[2]), lessThan(0.01));
    }

    @Test
    @DisplayName("Closer particles experience stronger repulsion")
    void falloff() {
        repulsion.addObstacle(new ObstacleRepulsion.Obstacle(0, 0, 0, 10, 5.0));

        double[] close = repulsion.calculateRepulsion(3, 0, 0, 0, 0, 0);
        double[] far = repulsion.calculateRepulsion(15, 0, 0, 0, 0, 0);

        double closeMag = Math.sqrt(close[0] * close[0] + close[1] * close[1] + close[2] * close[2]);
        double farMag = Math.sqrt(far[0] * far[0] + far[1] * far[1] + far[2] * far[2]);

        assertThat("Closer particle should experience stronger repulsion",
                closeMag, greaterThan(farMag));
    }

    @Test
    @DisplayName("Particle outside effective radius is not affected")
    void outsideRadius() {
        repulsion.addObstacle(new ObstacleRepulsion.Obstacle(0, 0, 0, 10, 5.0));

        // Particle at (25, 0, 0) - outside effective radius of 20
        double[] result = repulsion.calculateRepulsion(25, 0, 0, 0, 0, 0);

        assertEquals(0.0, result[0], 1e-10);
        assertEquals(0.0, result[1], 1e-10);
        assertEquals(0.0, result[2], 1e-10);
    }

    @Test
    @DisplayName("Tangential swirl is perpendicular to radial direction")
    void tangentialSwirl() {
        repulsion.addObstacle(new ObstacleRepulsion.Obstacle(0, 0, 0, 10, 5.0));

        // Particle at (5, 0, 0) with curl in Y direction
        double[] result = repulsion.calculateRepulsion(5, 0, 0, 0, 1, 0);

        // With radial direction = (1, 0, 0) and curl = (0, 1, 0),
        // tangential = cross(radial, curl) = (0, 0, 1)
        // So we expect some Z component from the swirl
        assertThat("Tangential swirl should add Z component", Math.abs(result[2]), greaterThan(0.0));
    }

    @Test
    @DisplayName("No obstacles means no repulsion")
    void noObstacles() {
        double[] result = repulsion.calculateRepulsion(5, 0, 0, 1, 0, 0);
        assertEquals(0.0, result[0], 1e-10);
        assertEquals(0.0, result[1], 1e-10);
        assertEquals(0.0, result[2], 1e-10);
    }

    @Test
    @DisplayName("Multiple obstacles compound their effects")
    void multipleObstacles() {
        repulsion.addObstacle(new ObstacleRepulsion.Obstacle(-5, 0, 0, 10, 5.0));
        repulsion.addObstacle(new ObstacleRepulsion.Obstacle(5, 0, 0, 10, 5.0));

        // Particle at (0, 0, 0) - equidistant from both obstacles
        // Repulsions should roughly cancel in X, but both push away
        double[] result = repulsion.calculateRepulsion(0, 0, 0, 0, 0, 0);

        // X component should be near zero (opposing forces cancel)
        assertThat("X repulsions should roughly cancel", Math.abs(result[0]), lessThan(0.5));
    }

    @Test
    @DisplayName("Add and remove obstacles works correctly")
    void addRemoveObstacles() {
        ObstacleRepulsion.Obstacle obs = new ObstacleRepulsion.Obstacle(0, 0, 0, 10, 5.0);
        repulsion.addObstacle(obs);
        assertThat(repulsion.getObstacles(), hasSize(1));

        repulsion.removeObstacle(obs);
        assertThat(repulsion.getObstacles(), hasSize(0));
    }

    @Test
    @DisplayName("Clear obstacles removes all")
    void clearObstacles() {
        repulsion.addObstacle(new ObstacleRepulsion.Obstacle(0, 0, 0, 10, 5.0));
        repulsion.addObstacle(new ObstacleRepulsion.Obstacle(10, 0, 0, 5, 3.0));
        assertThat(repulsion.getObstacles(), hasSize(2));

        repulsion.clearObstacles();
        assertThat(repulsion.getObstacles(), hasSize(0));
    }
}
