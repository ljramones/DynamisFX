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
import org.dynamisfx.particlefields.linear.*;
import org.dynamisfx.particlefields.orbital.*;
import org.dynamisfx.particlefields.vortex.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for all ParticleFieldGenerator implementations.
 */
@DisplayName("ParticleFieldGenerator implementations")
public class ParticleFieldGeneratorTest {

    private static final int TEST_ELEMENT_COUNT = 100;
    private static final Random TEST_RANDOM = new Random(42);

    @Nested
    @DisplayName("PlanetaryRingGenerator")
    class PlanetaryRing {
        @Test
        @DisplayName("generates correct type")
        void correctType() {
            PlanetaryRingGenerator gen = new PlanetaryRingGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.PLANETARY_RING));
        }

        @Test
        @DisplayName("generates correct count")
        void correctCount() {
            List<ParticleFieldElement> elements = generateOrbital(ParticleFieldType.PLANETARY_RING);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
        }

        @Test
        @DisplayName("elements have ORBITAL motion model")
        void orbitalMotion() {
            List<ParticleFieldElement> elements = generateOrbital(ParticleFieldType.PLANETARY_RING);
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.ORBITAL)));
        }

        @Test
        @DisplayName("elements have valid sizes")
        void validSizes() {
            List<ParticleFieldElement> elements = generateOrbital(ParticleFieldType.PLANETARY_RING);
            elements.forEach(e -> assertThat(e.getSize(), greaterThan(0.0)));
        }
    }

    @Nested
    @DisplayName("AsteroidBeltGenerator")
    class AsteroidBelt {
        @Test
        @DisplayName("generates correct type and count")
        void correctTypeAndCount() {
            AsteroidBeltGenerator gen = new AsteroidBeltGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.ASTEROID_BELT));
            List<ParticleFieldElement> elements = generateOrbital(ParticleFieldType.ASTEROID_BELT);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
        }
    }

    @Nested
    @DisplayName("DebrisDiskGenerator")
    class DebrisDisk {
        @Test
        @DisplayName("generates correct type and count")
        void correctTypeAndCount() {
            DebrisDiskGenerator gen = new DebrisDiskGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.DEBRIS_DISK));
            List<ParticleFieldElement> elements = generateOrbital(ParticleFieldType.DEBRIS_DISK);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
        }
    }

    @Nested
    @DisplayName("DustCloudGenerator")
    class DustCloud {
        @Test
        @DisplayName("generates correct type and count")
        void correctTypeAndCount() {
            DustCloudGenerator gen = new DustCloudGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.DUST_CLOUD));
            List<ParticleFieldElement> elements = generateOrbital(ParticleFieldType.DUST_CLOUD);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
        }
    }

    @Nested
    @DisplayName("AccretionDiskGenerator")
    class AccretionDisk {
        @Test
        @DisplayName("generates correct type and count")
        void correctTypeAndCount() {
            AccretionDiskGenerator gen = new AccretionDiskGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.ACCRETION_DISK));
            List<ParticleFieldElement> elements = generateOrbital(ParticleFieldType.ACCRETION_DISK);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
        }
    }

    @Nested
    @DisplayName("RainGenerator")
    class Rain {
        @Test
        @DisplayName("generates correct type")
        void correctType() {
            RainGenerator gen = new RainGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.RAIN));
        }

        @Test
        @DisplayName("generates correct count with LINEAR motion")
        void correctCountAndMotion() {
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.RAIN);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }

        @Test
        @DisplayName("elements have finite lifetime")
        void finiteLifetime() {
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.RAIN);
            elements.forEach(e -> assertThat(e.getLifetime(), greaterThan(0.0)));
        }
    }

    @Nested
    @DisplayName("FireGenerator")
    class Fire {
        @Test
        @DisplayName("generates correct type and LINEAR motion")
        void correctTypeAndMotion() {
            FireGenerator gen = new FireGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.FIRE));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.FIRE);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("ExplosionGenerator")
    class Explosion {
        @Test
        @DisplayName("generates correct type and LINEAR motion")
        void correctTypeAndMotion() {
            ExplosionGenerator gen = new ExplosionGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.EXPLOSION));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.EXPLOSION);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("StarfieldGenerator")
    class Starfield {
        @Test
        @DisplayName("generates correct type and LINEAR motion")
        void correctTypeAndMotion() {
            StarfieldGenerator gen = new StarfieldGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.STARFIELD));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.STARFIELD);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }

        @Test
        @DisplayName("elements have infinite lifetime")
        void infiniteLifetime() {
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.STARFIELD);
            elements.forEach(e -> assertThat(e.getLifetime(), is(-1.0)));
        }
    }

    @Nested
    @DisplayName("SwarmGenerator")
    class Swarm {
        @Test
        @DisplayName("generates correct type and LINEAR motion")
        void correctTypeAndMotion() {
            SwarmGenerator gen = new SwarmGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.SWARM));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.SWARM);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    // ========== New Linear Generator Tests ==========

    @Nested
    @DisplayName("SnowGenerator")
    class Snow {
        @Test
        @DisplayName("generates correct type, count, and LINEAR motion")
        void correctTypeCountAndMotion() {
            SnowGenerator gen = new SnowGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.SNOW));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.SNOW);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("FogGenerator")
    class Fog {
        @Test
        @DisplayName("generates correct type, count, and LINEAR motion")
        void correctTypeCountAndMotion() {
            FogGenerator gen = new FogGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.FOG));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.FOG);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("SmokeGenerator")
    class Smoke {
        @Test
        @DisplayName("generates correct type, count, and LINEAR motion")
        void correctTypeCountAndMotion() {
            SmokeGenerator gen = new SmokeGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.SMOKE));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.SMOKE);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("SparksGenerator")
    class Sparks {
        @Test
        @DisplayName("generates correct type, count, and LINEAR motion")
        void correctTypeCountAndMotion() {
            SparksGenerator gen = new SparksGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.SPARKS));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.SPARKS);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("LeavesGenerator")
    class Leaves {
        @Test
        @DisplayName("generates correct type, count, and LINEAR motion")
        void correctTypeCountAndMotion() {
            LeavesGenerator gen = new LeavesGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.LEAVES));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.LEAVES);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("CloudsGenerator")
    class Clouds {
        @Test
        @DisplayName("generates correct type, count, and LINEAR motion")
        void correctTypeCountAndMotion() {
            CloudsGenerator gen = new CloudsGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.CLOUDS));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.CLOUDS);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("LightningGenerator")
    class Lightning {
        @Test
        @DisplayName("generates correct type, count, and LINEAR motion")
        void correctTypeCountAndMotion() {
            LightningGenerator gen = new LightningGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.LIGHTNING));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.LIGHTNING);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("WaterSplashGenerator")
    class WaterSplash {
        @Test
        @DisplayName("generates correct type, count, and LINEAR motion")
        void correctTypeCountAndMotion() {
            WaterSplashGenerator gen = new WaterSplashGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.WATER_SPLASH));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.WATER_SPLASH);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("ConfettiGenerator")
    class Confetti {
        @Test
        @DisplayName("generates correct type, count, and LINEAR motion")
        void correctTypeCountAndMotion() {
            ConfettiGenerator gen = new ConfettiGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.CONFETTI));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.CONFETTI);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    @Nested
    @DisplayName("DustMotesGenerator")
    class DustMotes {
        @Test
        @DisplayName("generates correct type, count, and LINEAR motion")
        void correctTypeCountAndMotion() {
            DustMotesGenerator gen = new DustMotesGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.DUST_MOTES));
            List<ParticleFieldElement> elements = generateLinear(ParticleFieldType.DUST_MOTES);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.LINEAR)));
        }
    }

    // ========== Vortex Generator Tests ==========

    @Nested
    @DisplayName("PortalGenerator")
    class Portal {
        @Test
        @DisplayName("generates correct type, count, and VORTEX motion")
        void correctTypeCountAndMotion() {
            PortalGenerator gen = new PortalGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.PORTAL));
            List<ParticleFieldElement> elements = generateVortex(ParticleFieldType.PORTAL);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.VORTEX)));
        }
    }

    @Nested
    @DisplayName("MagicAuraGenerator")
    class MagicAura {
        @Test
        @DisplayName("generates correct type, count, and VORTEX motion")
        void correctTypeCountAndMotion() {
            MagicAuraGenerator gen = new MagicAuraGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.MAGIC_AURA));
            List<ParticleFieldElement> elements = generateVortex(ParticleFieldType.MAGIC_AURA);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.VORTEX)));
        }
    }

    @Nested
    @DisplayName("BubblesGenerator")
    class Bubbles {
        @Test
        @DisplayName("generates correct type, count, and VORTEX motion")
        void correctTypeCountAndMotion() {
            BubblesGenerator gen = new BubblesGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.BUBBLES));
            List<ParticleFieldElement> elements = generateVortex(ParticleFieldType.BUBBLES);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.VORTEX)));
        }
    }

    @Nested
    @DisplayName("WavesGenerator")
    class Waves {
        @Test
        @DisplayName("generates correct type, count, and VORTEX motion")
        void correctTypeCountAndMotion() {
            WavesGenerator gen = new WavesGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.WAVES));
            List<ParticleFieldElement> elements = generateVortex(ParticleFieldType.WAVES);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.VORTEX)));
        }
    }

    @Nested
    @DisplayName("EnergyBeamGenerator")
    class EnergyBeam {
        @Test
        @DisplayName("generates correct type, count, and VORTEX motion")
        void correctTypeCountAndMotion() {
            EnergyBeamGenerator gen = new EnergyBeamGenerator();
            assertThat(gen.getFieldType(), is(ParticleFieldType.ENERGY_BEAM));
            List<ParticleFieldElement> elements = generateVortex(ParticleFieldType.ENERGY_BEAM);
            assertThat(elements, hasSize(TEST_ELEMENT_COUNT));
            elements.forEach(e -> assertThat(e.getMotionModel(), is(MotionModel.VORTEX)));
        }
    }

    // Helper methods

    private List<ParticleFieldElement> generateVortex(ParticleFieldType type) {
        ParticleFieldConfiguration config = ParticleFieldConfiguration.builder()
                .type(type)
                .numElements(TEST_ELEMENT_COUNT)
                .innerRadius(5)
                .outerRadius(30)
                .minSize(0.1)
                .maxSize(0.5)
                .primaryColor(Color.WHITE)
                .secondaryColor(Color.GRAY)
                .drag(0.1)
                .minLifetime(1.0)
                .maxLifetime(3.0)
                .vortexAngularSpeed(2.0)
                .vortexRadialSpeed(-1.0)
                .vortexVerticalSpeed(0.5)
                .vortexTightness(1.0)
                .vortexHeight(30)
                .build();
        return ParticleFieldFactory.getGenerator(type).generate(config, new Random(42));
    }

    private List<ParticleFieldElement> generateOrbital(ParticleFieldType type) {
        ParticleFieldConfiguration config = ParticleFieldConfiguration.builder()
                .type(type)
                .numElements(TEST_ELEMENT_COUNT)
                .innerRadius(10)
                .outerRadius(50)
                .minSize(0.5)
                .maxSize(2.0)
                .thickness(5.0)
                .maxInclinationDeg(10.0)
                .maxEccentricity(0.05)
                .baseAngularSpeed(0.002)
                .centralBodyRadius(5)
                .primaryColor(Color.WHITE)
                .secondaryColor(Color.GRAY)
                .seed(42L)
                .build();
        return ParticleFieldFactory.getGenerator(type).generate(config, new Random(42));
    }

    private List<ParticleFieldElement> generateLinear(ParticleFieldType type) {
        ParticleFieldConfiguration config = ParticleFieldConfiguration.builder()
                .type(type)
                .numElements(TEST_ELEMENT_COUNT)
                .outerRadius(50)
                .minSize(0.1)
                .maxSize(0.5)
                .primaryColor(Color.WHITE)
                .secondaryColor(Color.GRAY)
                .gravity(new double[]{0, -9.8, 0})
                .drag(0.1)
                .minLifetime(1.0)
                .maxLifetime(3.0)
                .minSpeed(1.0)
                .maxSpeed(5.0)
                .spreadAngle(30)
                .emitterSize(new double[]{10, 10, 10})
                .build();
        return ParticleFieldFactory.getGenerator(type).generate(config, new Random(42));
    }
}
