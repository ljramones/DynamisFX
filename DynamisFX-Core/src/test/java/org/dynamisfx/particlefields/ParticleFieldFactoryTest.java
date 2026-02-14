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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for ParticleFieldFactory.
 */
@DisplayName("ParticleFieldFactory")
public class ParticleFieldFactoryTest {

    @Nested
    @DisplayName("Generator registry")
    class GeneratorRegistry {
        @Test
        @DisplayName("has generators for all 25 types")
        void allTypesRegistered() {
            for (ParticleFieldType type : ParticleFieldType.values()) {
                ParticleFieldGenerator gen = ParticleFieldFactory.getGenerator(type);
                assertThat(gen, notNullValue());
                assertThat(gen.getFieldType(), is(type));
            }
        }
    }

    @Nested
    @DisplayName("Presets")
    class Presets {
        @Test
        @DisplayName("has 33 preset names")
        void hasAllPresets() {
            String[] names = ParticleFieldFactory.getPresetNames();
            assertThat(names, arrayWithSize(33));
        }

        @Test
        @DisplayName("all presets load successfully")
        void allPresetsLoad() {
            for (String name : ParticleFieldFactory.getPresetNames()) {
                ParticleFieldConfiguration config = ParticleFieldFactory.getPreset(name);
                assertThat(config, notNullValue());
                assertThat(config.name(), not(emptyOrNullString()));
                assertThat(config.numElements(), greaterThan(0));
            }
        }

        @Test
        @DisplayName("unknown preset throws")
        void unknownPresetThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> ParticleFieldFactory.getPreset("NonexistentPreset"));
        }

        @Test
        @DisplayName("nebula presets are subset")
        void nebulaPresetsSubset() {
            String[] nebulaPresets = ParticleFieldFactory.getNebulaPresetNames();
            assertThat(nebulaPresets, arrayWithSize(5));
            for (String name : nebulaPresets) {
                ParticleFieldConfiguration config = ParticleFieldFactory.getPreset(name);
                assertThat(config.type(), is(ParticleFieldType.DUST_CLOUD));
            }
        }

        @Test
        @DisplayName("linear presets are subset")
        void linearPresetsSubset() {
            String[] linearPresets = ParticleFieldFactory.getLinearPresetNames();
            assertThat(linearPresets, arrayWithSize(15));
            for (String name : linearPresets) {
                ParticleFieldConfiguration config = ParticleFieldFactory.getPreset(name);
                assertThat(config.type().getMotionModel(), is(MotionModel.LINEAR));
            }
        }

        @Test
        @DisplayName("vortex presets are subset")
        void vortexPresetsSubset() {
            String[] vortexPresets = ParticleFieldFactory.getVortexPresetNames();
            assertThat(vortexPresets, arrayWithSize(5));
            for (String name : vortexPresets) {
                ParticleFieldConfiguration config = ParticleFieldFactory.getPreset(name);
                assertThat(config.type().getMotionModel(), is(MotionModel.VORTEX));
            }
        }
    }

    @Nested
    @DisplayName("generateElements")
    class GenerateElements {
        @Test
        @DisplayName("generates valid elements from preset")
        void generatesValidElements() {
            ParticleFieldConfiguration config = ParticleFieldFactory.saturnRing();
            List<ParticleFieldElement> elements = ParticleFieldFactory.generateElements(config, new Random(42));

            assertThat(elements, hasSize(config.numElements()));
            elements.forEach(e -> {
                assertThat(e.getSize(), greaterThan(0.0));
                assertThat(e.getColor(), notNullValue());
            });
        }

        @Test
        @DisplayName("default random seed works")
        void defaultSeed() {
            ParticleFieldConfiguration config = ParticleFieldFactory.saturnRing()
                    .withNumElements(100);
            List<ParticleFieldElement> elements = ParticleFieldFactory.generateElements(config);
            assertThat(elements, hasSize(100));
        }
    }
}
