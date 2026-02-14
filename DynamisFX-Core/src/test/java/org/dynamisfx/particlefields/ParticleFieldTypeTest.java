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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for ParticleFieldType enum.
 */
@DisplayName("ParticleFieldType")
public class ParticleFieldTypeTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {
        @Test
        @DisplayName("has all 25 types")
        void hasAllTypes() {
            assertThat(ParticleFieldType.values(), arrayWithSize(25));
        }

        @Test
        @DisplayName("orbital types exist")
        void orbitalTypesExist() {
            assertThat(ParticleFieldType.valueOf("PLANETARY_RING"), notNullValue());
            assertThat(ParticleFieldType.valueOf("ASTEROID_BELT"), notNullValue());
            assertThat(ParticleFieldType.valueOf("DEBRIS_DISK"), notNullValue());
            assertThat(ParticleFieldType.valueOf("DUST_CLOUD"), notNullValue());
            assertThat(ParticleFieldType.valueOf("ACCRETION_DISK"), notNullValue());
        }

        @Test
        @DisplayName("linear types exist")
        void linearTypesExist() {
            assertThat(ParticleFieldType.valueOf("RAIN"), notNullValue());
            assertThat(ParticleFieldType.valueOf("FIRE"), notNullValue());
            assertThat(ParticleFieldType.valueOf("EXPLOSION"), notNullValue());
            assertThat(ParticleFieldType.valueOf("STARFIELD"), notNullValue());
            assertThat(ParticleFieldType.valueOf("SWARM"), notNullValue());
        }

        @Test
        @DisplayName("new linear types exist")
        void newLinearTypesExist() {
            assertThat(ParticleFieldType.valueOf("SNOW"), notNullValue());
            assertThat(ParticleFieldType.valueOf("FOG"), notNullValue());
            assertThat(ParticleFieldType.valueOf("SMOKE"), notNullValue());
            assertThat(ParticleFieldType.valueOf("SPARKS"), notNullValue());
            assertThat(ParticleFieldType.valueOf("LEAVES"), notNullValue());
            assertThat(ParticleFieldType.valueOf("CLOUDS"), notNullValue());
            assertThat(ParticleFieldType.valueOf("LIGHTNING"), notNullValue());
            assertThat(ParticleFieldType.valueOf("WATER_SPLASH"), notNullValue());
            assertThat(ParticleFieldType.valueOf("CONFETTI"), notNullValue());
            assertThat(ParticleFieldType.valueOf("DUST_MOTES"), notNullValue());
        }

        @Test
        @DisplayName("vortex types exist")
        void vortexTypesExist() {
            assertThat(ParticleFieldType.valueOf("PORTAL"), notNullValue());
            assertThat(ParticleFieldType.valueOf("MAGIC_AURA"), notNullValue());
            assertThat(ParticleFieldType.valueOf("BUBBLES"), notNullValue());
            assertThat(ParticleFieldType.valueOf("WAVES"), notNullValue());
            assertThat(ParticleFieldType.valueOf("ENERGY_BEAM"), notNullValue());
        }
    }

    @Nested
    @DisplayName("Display names")
    class DisplayNames {
        @Test
        @DisplayName("orbital types have display names")
        void orbitalDisplayNames() {
            assertThat(ParticleFieldType.PLANETARY_RING.getDisplayName(), is("Planetary Ring"));
            assertThat(ParticleFieldType.ASTEROID_BELT.getDisplayName(), is("Asteroid Belt"));
            assertThat(ParticleFieldType.DEBRIS_DISK.getDisplayName(), is("Debris Disk"));
            assertThat(ParticleFieldType.DUST_CLOUD.getDisplayName(), is("Dust Cloud"));
            assertThat(ParticleFieldType.ACCRETION_DISK.getDisplayName(), is("Accretion Disk"));
        }

        @Test
        @DisplayName("linear types have display names")
        void linearDisplayNames() {
            assertThat(ParticleFieldType.RAIN.getDisplayName(), is("Rain"));
            assertThat(ParticleFieldType.FIRE.getDisplayName(), is("Fire"));
            assertThat(ParticleFieldType.EXPLOSION.getDisplayName(), is("Explosion"));
            assertThat(ParticleFieldType.STARFIELD.getDisplayName(), is("Starfield"));
            assertThat(ParticleFieldType.SWARM.getDisplayName(), is("Swarm"));
        }

        @Test
        @DisplayName("vortex types have display names")
        void vortexDisplayNames() {
            assertThat(ParticleFieldType.PORTAL.getDisplayName(), is("Portal"));
            assertThat(ParticleFieldType.MAGIC_AURA.getDisplayName(), is("Magic Aura"));
            assertThat(ParticleFieldType.BUBBLES.getDisplayName(), is("Bubbles"));
            assertThat(ParticleFieldType.WAVES.getDisplayName(), is("Waves"));
            assertThat(ParticleFieldType.ENERGY_BEAM.getDisplayName(), is("Energy Beam"));
        }
    }

    @Nested
    @DisplayName("Motion model mapping")
    class MotionModelMapping {
        @Test
        @DisplayName("orbital types map to ORBITAL")
        void orbitalTypesMapToOrbital() {
            assertThat(ParticleFieldType.PLANETARY_RING.getMotionModel(), is(MotionModel.ORBITAL));
            assertThat(ParticleFieldType.ASTEROID_BELT.getMotionModel(), is(MotionModel.ORBITAL));
            assertThat(ParticleFieldType.DEBRIS_DISK.getMotionModel(), is(MotionModel.ORBITAL));
            assertThat(ParticleFieldType.DUST_CLOUD.getMotionModel(), is(MotionModel.ORBITAL));
            assertThat(ParticleFieldType.ACCRETION_DISK.getMotionModel(), is(MotionModel.ORBITAL));
        }

        @Test
        @DisplayName("linear types map to LINEAR")
        void linearTypesMapToLinear() {
            assertThat(ParticleFieldType.RAIN.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.FIRE.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.EXPLOSION.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.STARFIELD.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.SWARM.getMotionModel(), is(MotionModel.LINEAR));
        }

        @Test
        @DisplayName("new linear types map to LINEAR")
        void newLinearTypesMapToLinear() {
            assertThat(ParticleFieldType.SNOW.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.FOG.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.SMOKE.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.SPARKS.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.LEAVES.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.CLOUDS.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.LIGHTNING.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.WATER_SPLASH.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.CONFETTI.getMotionModel(), is(MotionModel.LINEAR));
            assertThat(ParticleFieldType.DUST_MOTES.getMotionModel(), is(MotionModel.LINEAR));
        }

        @Test
        @DisplayName("vortex types map to VORTEX")
        void vortexTypesMapToVortex() {
            assertThat(ParticleFieldType.PORTAL.getMotionModel(), is(MotionModel.VORTEX));
            assertThat(ParticleFieldType.MAGIC_AURA.getMotionModel(), is(MotionModel.VORTEX));
            assertThat(ParticleFieldType.BUBBLES.getMotionModel(), is(MotionModel.VORTEX));
            assertThat(ParticleFieldType.WAVES.getMotionModel(), is(MotionModel.VORTEX));
            assertThat(ParticleFieldType.ENERGY_BEAM.getMotionModel(), is(MotionModel.VORTEX));
        }
    }
}
