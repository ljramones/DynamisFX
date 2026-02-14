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

    // Helper methods

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
