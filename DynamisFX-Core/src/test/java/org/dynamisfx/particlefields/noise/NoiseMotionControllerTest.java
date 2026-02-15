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

import javafx.scene.paint.Color;
import org.dynamisfx.particlefields.ParticleFieldElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NoiseMotionController verifying displacement, determinism,
 * strength scaling, and density modulation.
 */
public class NoiseMotionControllerTest {

    private List<ParticleFieldElement> elements;

    @BeforeEach
    void setUp() {
        elements = new ArrayList<>();
        // Create a small grid of linear particles at known positions
        for (int i = 0; i < 10; i++) {
            elements.add(new ParticleFieldElement(
                    i * 10.0, 0, 0,     // position
                    0, 0, 0,             // velocity
                    0, 0, 0,             // acceleration
                    0,                    // drag
                    -1,                   // lifetime (infinite)
                    1.0,                  // size
                    Color.WHITE           // color
            ));
        }
    }

    @Nested
    @DisplayName("Curl Noise Displacement")
    class CurlDisplacementTests {

        @Test
        @DisplayName("Particles are displaced after applying noise motion")
        void particlesAreDisplaced() {
            NoiseMotionConfig config = NoiseMotionConfig.builder()
                    .curlStrength(1.0)
                    .curlFrequency(0.05)
                    .curlOctaves(2)
                    .timeSpeed(0.5)
                    .updateInterval(1)
                    .seed(42)
                    .build();

            NoiseMotionController controller = new NoiseMotionController(config);

            // Record initial velocities
            double[] initialVx = new double[elements.size()];
            for (int i = 0; i < elements.size(); i++) {
                initialVx[i] = elements.get(i).getVx();
            }

            controller.applyNoiseMotion(elements, 1.0);

            // At least some particles should have non-zero velocity changes
            boolean anyDisplaced = false;
            for (int i = 0; i < elements.size(); i++) {
                if (Math.abs(elements.get(i).getVx() - initialVx[i]) > 1e-10 ||
                    Math.abs(elements.get(i).getVy()) > 1e-10 ||
                    Math.abs(elements.get(i).getVz()) > 1e-10) {
                    anyDisplaced = true;
                    break;
                }
            }
            assertTrue(anyDisplaced, "At least some particles should be displaced by curl noise");
        }

        @Test
        @DisplayName("Same seed produces deterministic results")
        void deterministic() {
            NoiseMotionConfig config = NoiseMotionConfig.builder()
                    .curlStrength(0.5)
                    .curlFrequency(0.02)
                    .curlOctaves(3)
                    .timeSpeed(0.3)
                    .updateInterval(1)
                    .seed(123)
                    .build();

            // First run
            List<ParticleFieldElement> elements1 = createElements();
            NoiseMotionController c1 = new NoiseMotionController(config);
            c1.applyNoiseMotion(elements1, 1.0);

            // Second run with same config
            List<ParticleFieldElement> elements2 = createElements();
            NoiseMotionController c2 = new NoiseMotionController(config);
            c2.applyNoiseMotion(elements2, 1.0);

            for (int i = 0; i < elements1.size(); i++) {
                assertEquals(elements1.get(i).getVx(), elements2.get(i).getVx(), 1e-10,
                        "Vx should be deterministic for same seed");
                assertEquals(elements1.get(i).getVy(), elements2.get(i).getVy(), 1e-10,
                        "Vy should be deterministic for same seed");
                assertEquals(elements1.get(i).getVz(), elements2.get(i).getVz(), 1e-10,
                        "Vz should be deterministic for same seed");
            }
        }

        @Test
        @DisplayName("Displacement scales proportionally with curlStrength")
        void displacementProportionalToStrength() {
            NoiseMotionConfig weakConfig = NoiseMotionConfig.builder()
                    .curlStrength(0.1)
                    .curlFrequency(0.02)
                    .curlOctaves(2)
                    .timeSpeed(0.3)
                    .updateInterval(1)
                    .seed(42)
                    .build();

            NoiseMotionConfig strongConfig = NoiseMotionConfig.builder()
                    .curlStrength(1.0)
                    .curlFrequency(0.02)
                    .curlOctaves(2)
                    .timeSpeed(0.3)
                    .updateInterval(1)
                    .seed(42)
                    .build();

            List<ParticleFieldElement> weakElements = createElements();
            List<ParticleFieldElement> strongElements = createElements();

            new NoiseMotionController(weakConfig).applyNoiseMotion(weakElements, 1.0);
            new NoiseMotionController(strongConfig).applyNoiseMotion(strongElements, 1.0);

            // Strong should have ~10x the displacement of weak (strength ratio is 10:1)
            double weakMag = totalVelocityMagnitude(weakElements);
            double strongMag = totalVelocityMagnitude(strongElements);

            assertThat("Strong noise should produce greater displacement",
                    strongMag, greaterThan(weakMag * 5.0));
        }

        @Test
        @DisplayName("Noise time advances correctly")
        void noiseTimeAdvances() {
            NoiseMotionConfig config = NoiseMotionConfig.builder()
                    .curlStrength(0.5)
                    .timeSpeed(0.5)
                    .updateInterval(1)
                    .seed(42)
                    .build();

            NoiseMotionController controller = new NoiseMotionController(config);
            assertEquals(0.0, controller.getNoiseTime(), 1e-10);

            controller.applyNoiseMotion(elements, 1.0);
            assertEquals(0.5, controller.getNoiseTime(), 1e-10);

            controller.applyNoiseMotion(elements, 2.0);
            assertEquals(1.5, controller.getNoiseTime(), 1e-10);
        }

        @Test
        @DisplayName("Reset clears noise time and displacement state")
        void resetClearsState() {
            NoiseMotionConfig config = NoiseMotionConfig.fog();
            NoiseMotionController controller = new NoiseMotionController(config);

            controller.applyNoiseMotion(elements, 1.0);
            assertThat(controller.getNoiseTime(), greaterThan(0.0));

            controller.reset();
            assertEquals(0.0, controller.getNoiseTime(), 1e-10);
        }
    }

    @Nested
    @DisplayName("Density Modulation")
    class DensityTests {

        @Test
        @DisplayName("Opacity modulation stays within configured bounds")
        void opacityInRange() {
            double minOpacity = 0.2;
            double maxOpacity = 0.8;

            NoiseMotionConfig config = NoiseMotionConfig.builder()
                    .curlStrength(0.1)
                    .updateInterval(1)
                    .densityOpacityEnabled(true)
                    .densityFrequency(0.05)
                    .densityOctaves(2)
                    .densityMinOpacity(minOpacity)
                    .densityMaxOpacity(maxOpacity)
                    .seed(42)
                    .build();

            NoiseMotionController controller = new NoiseMotionController(config);
            controller.applyNoiseMotion(elements, 1.0);

            for (ParticleFieldElement e : elements) {
                double opacity = e.getDynamicOpacity();
                assertThat("Opacity should be >= min", opacity,
                        greaterThanOrEqualTo(minOpacity - 0.01));
                assertThat("Opacity should be <= max", opacity,
                        lessThanOrEqualTo(maxOpacity + 0.01));
            }
        }

        @Test
        @DisplayName("Scale modulation stays within configured bounds")
        void scaleInRange() {
            double minScale = 0.5;
            double maxScale = 2.0;

            NoiseMotionConfig config = NoiseMotionConfig.builder()
                    .curlStrength(0.1)
                    .updateInterval(1)
                    .densityScaleEnabled(true)
                    .densityFrequency(0.05)
                    .densityOctaves(2)
                    .densityMinScale(minScale)
                    .densityMaxScale(maxScale)
                    .seed(42)
                    .build();

            NoiseMotionController controller = new NoiseMotionController(config);
            controller.applyNoiseMotion(elements, 1.0);

            for (ParticleFieldElement e : elements) {
                double scale = e.getDynamicScale();
                assertThat("Scale should be >= min", scale,
                        greaterThanOrEqualTo(minScale - 0.01));
                assertThat("Scale should be <= max", scale,
                        lessThanOrEqualTo(maxScale + 0.01));
            }
        }

        @Test
        @DisplayName("Density disabled leaves default values unchanged")
        void densityDisabledLeavesDefaults() {
            NoiseMotionConfig config = NoiseMotionConfig.builder()
                    .curlStrength(0.5)
                    .updateInterval(1)
                    .densityOpacityEnabled(false)
                    .densityScaleEnabled(false)
                    .seed(42)
                    .build();

            NoiseMotionController controller = new NoiseMotionController(config);
            controller.applyNoiseMotion(elements, 1.0);

            for (ParticleFieldElement e : elements) {
                // dynamicOpacity -1 means "use color's opacity"
                assertEquals(1.0, e.getDynamicOpacity(), 1e-10,
                        "Opacity should remain at color default (1.0 for WHITE)");
                assertEquals(1.0, e.getDynamicScale(), 1e-10,
                        "Scale should remain at default 1.0");
            }
        }
    }

    @Nested
    @DisplayName("Preset Configs")
    class PresetTests {

        @Test
        @DisplayName("Fog preset has reasonable values")
        void fogPreset() {
            NoiseMotionConfig fog = NoiseMotionConfig.fog();
            assertThat(fog.curlStrength(), greaterThan(0.0));
            assertThat(fog.curlFrequency(), greaterThan(0.0));
            assertThat(fog.curlOctaves(), greaterThanOrEqualTo(1));
            assertTrue(fog.densityOpacityEnabled());
            assertTrue(fog.densityScaleEnabled());
        }

        @Test
        @DisplayName("Clouds preset has reasonable values")
        void cloudsPreset() {
            NoiseMotionConfig clouds = NoiseMotionConfig.clouds();
            assertThat(clouds.curlStrength(), greaterThan(0.0));
            assertThat(clouds.timeSpeed(), greaterThan(0.0));
            assertTrue(clouds.densityOpacityEnabled());
        }

        @Test
        @DisplayName("Smoke preset has higher turbulence than fog")
        void smokePreset() {
            NoiseMotionConfig smoke = NoiseMotionConfig.smoke();
            NoiseMotionConfig fog = NoiseMotionConfig.fog();
            assertThat("Smoke should have stronger curl", smoke.curlStrength(),
                    greaterThan(fog.curlStrength()));
            assertThat("Smoke should have faster time", smoke.timeSpeed(),
                    greaterThan(fog.timeSpeed()));
        }
    }

    // ==================== Helper methods ====================

    private List<ParticleFieldElement> createElements() {
        List<ParticleFieldElement> result = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            result.add(new ParticleFieldElement(
                    i * 10.0, 0, 0,
                    0, 0, 0,
                    0, 0, 0,
                    0, -1, 1.0, Color.WHITE
            ));
        }
        return result;
    }

    private double totalVelocityMagnitude(List<ParticleFieldElement> elems) {
        double total = 0;
        for (ParticleFieldElement e : elems) {
            total += Math.sqrt(e.getVx() * e.getVx() + e.getVy() * e.getVy() + e.getVz() * e.getVz());
        }
        return total;
    }
}
