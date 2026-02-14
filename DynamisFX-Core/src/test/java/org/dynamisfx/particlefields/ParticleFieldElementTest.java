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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for ParticleFieldElement.
 */
@DisplayName("ParticleFieldElement")
public class ParticleFieldElementTest {

    @Nested
    @DisplayName("Orbital construction")
    class OrbitalConstruction {
        @Test
        @DisplayName("creates orbital element with correct motion model")
        void motionModel() {
            ParticleFieldElement e = createOrbitalElement(10, 0, 0);
            assertThat(e.getMotionModel(), is(MotionModel.ORBITAL));
        }

        @Test
        @DisplayName("stores orbital parameters")
        void storesParams() {
            ParticleFieldElement e = createOrbitalElement(10, 0.05, 0.1);
            assertThat(e.getSemiMajorAxis(), is(10.0));
            assertThat(e.getEccentricity(), is(0.05));
            assertThat(e.getInclination(), is(0.1));
            assertThat(e.getSize(), is(1.0));
            assertThat(e.getColor(), is(Color.WHITE));
        }

        @Test
        @DisplayName("initializes position on construction")
        void initializesPosition() {
            ParticleFieldElement e = createOrbitalElement(10, 0, 0);
            // Circular orbit at angle 0 => x should be ~10, y ~0
            double r = Math.sqrt(e.getX() * e.getX() + e.getY() * e.getY() + e.getZ() * e.getZ());
            assertThat(r, closeTo(10.0, 1.0));
        }

        @Test
        @DisplayName("orbital element is always alive")
        void alwaysAlive() {
            ParticleFieldElement e = createOrbitalElement(10, 0, 0);
            assertThat(e.isAlive(), is(true));
            e.advance(1000);
            assertThat(e.isAlive(), is(true));
        }
    }

    @Nested
    @DisplayName("Orbital advance")
    class OrbitalAdvance {
        @Test
        @DisplayName("circular orbit maintains constant radius")
        void circularOrbitConstantRadius() {
            ParticleFieldElement e = createOrbitalElement(10, 0, 0);
            double r0 = Math.sqrt(e.getX() * e.getX() + e.getY() * e.getY() + e.getZ() * e.getZ());

            for (int i = 0; i < 100; i++) {
                e.advance(0.1);
                double r = Math.sqrt(e.getX() * e.getX() + e.getY() * e.getY() + e.getZ() * e.getZ());
                assertThat(r, closeTo(r0, 0.01));
            }
        }

        @Test
        @DisplayName("advance changes position")
        void advanceChangesPosition() {
            ParticleFieldElement e = createOrbitalElement(10, 0, 0);
            double x0 = e.getX();
            double z0 = e.getZ();
            e.advance(1.0);
            // Position should have changed
            boolean changed = Math.abs(e.getX() - x0) > 1e-6 || Math.abs(e.getZ() - z0) > 1e-6;
            assertThat(changed, is(true));
        }
    }

    @Nested
    @DisplayName("Linear construction")
    class LinearConstruction {
        @Test
        @DisplayName("creates linear element with correct motion model")
        void motionModel() {
            ParticleFieldElement e = createLinearElement();
            assertThat(e.getMotionModel(), is(MotionModel.LINEAR));
        }

        @Test
        @DisplayName("stores linear parameters")
        void storesParams() {
            ParticleFieldElement e = new ParticleFieldElement(
                    1, 2, 3,   // position
                    4, 5, 6,   // velocity
                    0, -9.8, 0, // acceleration
                    0.1,       // drag
                    5.0,       // lifetime
                    0.5,       // size
                    Color.RED
            );
            assertThat(e.getX(), is(1.0));
            assertThat(e.getY(), is(2.0));
            assertThat(e.getZ(), is(3.0));
            assertThat(e.getVx(), is(4.0));
            assertThat(e.getVy(), is(5.0));
            assertThat(e.getVz(), is(6.0));
            assertThat(e.getDrag(), is(0.1));
            assertThat(e.getLifetime(), is(5.0));
        }
    }

    @Nested
    @DisplayName("Linear advance")
    class LinearAdvance {
        @Test
        @DisplayName("velocity integrates into position")
        void velocityIntegration() {
            ParticleFieldElement e = new ParticleFieldElement(
                    0, 0, 0,   // position
                    10, 0, 0,  // velocity (10 in x)
                    0, 0, 0,   // no acceleration
                    0,         // no drag
                    -1,        // infinite lifetime
                    1.0,
                    Color.WHITE
            );

            e.advance(1.0);
            assertThat(e.getX(), closeTo(10.0, 0.01));
            assertThat(e.getY(), closeTo(0.0, 0.01));
        }

        @Test
        @DisplayName("acceleration changes velocity")
        void accelerationIntegration() {
            ParticleFieldElement e = new ParticleFieldElement(
                    0, 0, 0,    // position
                    0, 0, 0,    // zero velocity
                    10, 0, 0,   // 10 m/s^2 in x
                    0,          // no drag
                    -1,
                    1.0,
                    Color.WHITE
            );

            e.advance(1.0);
            // After 1s: v = 10, x = 10
            assertThat(e.getVx(), closeTo(10.0, 0.01));
            assertThat(e.getX(), closeTo(10.0, 0.01));
        }

        @Test
        @DisplayName("drag reduces velocity")
        void dragReducesVelocity() {
            ParticleFieldElement e = new ParticleFieldElement(
                    0, 0, 0,
                    10, 10, 10, // velocity
                    0, 0, 0,    // no acceleration
                    0.5,        // drag
                    -1,
                    1.0,
                    Color.WHITE
            );

            e.advance(1.0);
            // Velocity should be reduced by drag
            assertThat(e.getVx(), lessThan(10.0));
            assertThat(e.getVy(), lessThan(10.0));
            assertThat(e.getVz(), lessThan(10.0));
        }
    }

    @Nested
    @DisplayName("isAlive / lifetime")
    class IsAlive {
        @Test
        @DisplayName("infinite lifetime stays alive")
        void infiniteLifetime() {
            ParticleFieldElement e = new ParticleFieldElement(
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 1.0, Color.WHITE
            );
            assertThat(e.isAlive(), is(true));
            e.advance(1000);
            assertThat(e.isAlive(), is(true));
        }

        @Test
        @DisplayName("finite lifetime dies after expiry")
        void finiteLifetime() {
            ParticleFieldElement e = new ParticleFieldElement(
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2.0, 1.0, Color.WHITE
            );
            assertThat(e.isAlive(), is(true));
            e.advance(1.0);
            assertThat(e.isAlive(), is(true));
            e.advance(1.5); // age = 2.5 > lifetime 2.0
            assertThat(e.isAlive(), is(false));
        }
    }

    // Helper methods

    private ParticleFieldElement createOrbitalElement(double semiMajorAxis, double eccentricity, double inclination) {
        return new ParticleFieldElement(
                semiMajorAxis, eccentricity, inclination,
                0, 0,           // argumentOfPeriapsis, longitudeOfAscendingNode
                0, 0.01,        // initialAngle, angularSpeed
                1.0, 0,         // size, heightOffset
                Color.WHITE
        );
    }

    private ParticleFieldElement createLinearElement() {
        return new ParticleFieldElement(
                0, 0, 0,     // position
                1, 2, 3,     // velocity
                0, -9.8, 0,  // acceleration
                0.1,         // drag
                5.0,         // lifetime
                0.5,         // size
                Color.RED
        );
    }
}
