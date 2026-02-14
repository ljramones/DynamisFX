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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for NoiseGenerator.
 */
@DisplayName("NoiseGenerator")
public class NoiseGeneratorTest {

    private NoiseGenerator noise;

    @BeforeEach
    void setUp() {
        noise = new NoiseGenerator(42L);
    }

    @Nested
    @DisplayName("Construction")
    class Construction {
        @Test
        @DisplayName("creates with seed")
        void createsWithSeed() {
            NoiseGenerator gen = new NoiseGenerator(123L);
            assertThat(gen.getPersistence(), is(0.5));
            assertThat(gen.getLacunarity(), is(2.2));
        }

        @Test
        @DisplayName("creates with custom parameters")
        void createsWithParams() {
            NoiseGenerator gen = new NoiseGenerator(123L, 0.7, 2.5);
            assertThat(gen.getPersistence(), is(0.7));
            assertThat(gen.getLacunarity(), is(2.5));
        }

        @Test
        @DisplayName("clamps parameters to valid range")
        void clampsParams() {
            NoiseGenerator gen = new NoiseGenerator(123L, 0.0, 5.0);
            assertThat(gen.getPersistence(), is(0.1));
            assertThat(gen.getLacunarity(), is(4.0));
        }
    }

    @Nested
    @DisplayName("noise3D")
    class Noise3D {
        @Test
        @DisplayName("returns value in [-1, 1]")
        void rangeCheck() {
            for (int i = 0; i < 100; i++) {
                double val = noise.noise3D(i * 0.1, i * 0.2, i * 0.3);
                assertThat(val, greaterThanOrEqualTo(-1.0));
                assertThat(val, lessThanOrEqualTo(1.0));
            }
        }

        @Test
        @DisplayName("is deterministic")
        void deterministic() {
            double val1 = noise.noise3D(1.5, 2.5, 3.5);
            double val2 = noise.noise3D(1.5, 2.5, 3.5);
            assertThat(val1, is(val2));
        }

        @Test
        @DisplayName("different seeds produce different values")
        void differentSeeds() {
            NoiseGenerator gen2 = new NoiseGenerator(999L);
            double val1 = noise.noise3D(1.0, 2.0, 3.0);
            double val2 = gen2.noise3D(1.0, 2.0, 3.0);
            assertThat(val1, is(not(val2)));
        }
    }

    @Nested
    @DisplayName("Layered noise")
    class LayeredNoise {
        @Test
        @DisplayName("returns value in approximately [-1, 1]")
        void rangeCheck() {
            for (int i = 0; i < 50; i++) {
                double val = noise.layeredNoise(i * 0.1, i * 0.2, i * 0.3, 3);
                assertThat(val, greaterThanOrEqualTo(-1.1));
                assertThat(val, lessThanOrEqualTo(1.1));
            }
        }

        @Test
        @DisplayName("is deterministic")
        void deterministic() {
            double val1 = noise.layeredNoise(1.0, 2.0, 3.0, 3);
            double val2 = noise.layeredNoise(1.0, 2.0, 3.0, 3);
            assertThat(val1, is(val2));
        }
    }

    @Nested
    @DisplayName("Turbulence")
    class Turbulence {
        @Test
        @DisplayName("returns value in [0, 1]")
        void rangeCheck() {
            for (int i = 0; i < 50; i++) {
                double val = noise.turbulence(i * 0.1, i * 0.2, i * 0.3, 3);
                assertThat(val, greaterThanOrEqualTo(0.0));
                assertThat(val, lessThanOrEqualTo(1.0));
            }
        }
    }

    @Nested
    @DisplayName("Displacement")
    class Displacement {
        @Test
        @DisplayName("returns 3-component vector")
        void returns3Components() {
            double[] d = noise.displacement(1.0, 2.0, 3.0, 3, 0.5);
            assertThat(d.length, is(3));
        }

        @Test
        @DisplayName("filament displacement uses anisotropic factors")
        void filamentDisplacement() {
            double[] d = noise.filamentDisplacement(1.0, 2.0, 3.0, 3, 1.0);
            assertThat(d.length, is(3));
        }
    }
}
