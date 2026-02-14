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
 * Tests for ParticleFieldConfiguration.
 */
@DisplayName("ParticleFieldConfiguration")
public class ParticleFieldConfigurationTest {

    @Nested
    @DisplayName("Builder defaults")
    class BuilderDefaults {
        @Test
        @DisplayName("default orbital values")
        void defaultOrbitalValues() {
            ParticleFieldConfiguration config = ParticleFieldConfiguration.builder().build();
            assertThat(config.type(), is(ParticleFieldType.ASTEROID_BELT));
            assertThat(config.innerRadius(), is(50.0));
            assertThat(config.outerRadius(), is(100.0));
            assertThat(config.numElements(), is(5000));
            assertThat(config.name(), is("Particle Field"));
        }

        @Test
        @DisplayName("default linear values")
        void defaultLinearValues() {
            ParticleFieldConfiguration config = ParticleFieldConfiguration.builder().build();
            assertThat(config.gravity(), is(new double[]{0, -9.8, 0}));
            assertThat(config.wind(), is(new double[]{0, 0, 0}));
            assertThat(config.drag(), is(0.0));
            assertThat(config.minLifetime(), is(-1.0));
            assertThat(config.maxLifetime(), is(-1.0));
            assertThat(config.minSpeed(), is(0.0));
            assertThat(config.maxSpeed(), is(0.0));
            assertThat(config.spreadAngle(), is(0.0));
            assertThat(config.emitterSize(), is(new double[]{0, 0, 0}));
        }

        @Test
        @DisplayName("default nebula values")
        void defaultNebulaValues() {
            ParticleFieldConfiguration config = ParticleFieldConfiguration.builder().build();
            assertThat(config.radialPower(), is(0.5));
            assertThat(config.noiseStrength(), is(0.0));
            assertThat(config.noiseOctaves(), is(3));
            assertThat(config.colorGradientMode(), is(ColorGradientMode.LINEAR));
        }
    }

    @Nested
    @DisplayName("Fluent API")
    class FluentApi {
        @Test
        @DisplayName("builder sets all fields")
        void builderSetsAll() {
            ParticleFieldConfiguration config = ParticleFieldConfiguration.builder()
                    .type(ParticleFieldType.RAIN)
                    .numElements(1000)
                    .minSize(0.1)
                    .maxSize(0.5)
                    .primaryColor(Color.BLUE)
                    .name("Test Rain")
                    .gravity(new double[]{0, -5.0, 0})
                    .drag(0.3)
                    .minLifetime(1.0)
                    .maxLifetime(3.0)
                    .minSpeed(5.0)
                    .maxSpeed(10.0)
                    .spreadAngle(15.0)
                    .emitterSize(new double[]{10, 20, 10})
                    .build();

            assertThat(config.type(), is(ParticleFieldType.RAIN));
            assertThat(config.numElements(), is(1000));
            assertThat(config.name(), is("Test Rain"));
            assertThat(config.drag(), is(0.3));
            assertThat(config.minLifetime(), is(1.0));
            assertThat(config.maxLifetime(), is(3.0));
            assertThat(config.spreadAngle(), is(15.0));
        }
    }

    @Nested
    @DisplayName("with* copy methods")
    class WithMethods {
        @Test
        @DisplayName("withSeed creates copy with new seed")
        void withSeed() {
            ParticleFieldConfiguration config = ParticleFieldConfiguration.builder()
                    .seed(42L)
                    .name("Test")
                    .build();

            ParticleFieldConfiguration copy = config.withSeed(99L);
            assertThat(copy.seed(), is(99L));
            assertThat(copy.name(), is("Test"));
        }

        @Test
        @DisplayName("withNumElements creates copy with new count")
        void withNumElements() {
            ParticleFieldConfiguration config = ParticleFieldConfiguration.builder()
                    .numElements(5000)
                    .build();

            ParticleFieldConfiguration copy = config.withNumElements(1000);
            assertThat(copy.numElements(), is(1000));
            assertThat(copy.type(), is(config.type()));
        }

        @Test
        @DisplayName("withGlow creates copy with new glow settings")
        void withGlow() {
            ParticleFieldConfiguration config = ParticleFieldConfiguration.builder().build();

            ParticleFieldConfiguration copy = config.withGlow(true, 0.8);
            assertThat(copy.glowEnabled(), is(true));
            assertThat(copy.glowIntensity(), is(0.8));
        }

        @Test
        @DisplayName("withNoiseOctaves creates copy with new octaves")
        void withNoiseOctaves() {
            ParticleFieldConfiguration config = ParticleFieldConfiguration.builder()
                    .noiseOctaves(3)
                    .build();

            ParticleFieldConfiguration copy = config.withNoiseOctaves(5);
            assertThat(copy.noiseOctaves(), is(5));
        }

        @Test
        @DisplayName("withNoiseStrength creates copy with new strength")
        void withNoiseStrength() {
            ParticleFieldConfiguration config = ParticleFieldConfiguration.builder()
                    .noiseStrength(0.3)
                    .build();

            ParticleFieldConfiguration copy = config.withNoiseStrength(0.7);
            assertThat(copy.noiseStrength(), is(0.7));
        }
    }
}
