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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Factory for creating particle field configurations and generators.
 * Provides preset configurations for common particle field types and manages generator creation.
 */
public class ParticleFieldFactory {

    private static final Map<ParticleFieldType, ParticleFieldGenerator> generators =
            new EnumMap<>(ParticleFieldType.class);

    static {
        // Orbital generators
        generators.put(ParticleFieldType.PLANETARY_RING, new PlanetaryRingGenerator());
        generators.put(ParticleFieldType.ASTEROID_BELT, new AsteroidBeltGenerator());
        generators.put(ParticleFieldType.DEBRIS_DISK, new DebrisDiskGenerator());
        generators.put(ParticleFieldType.DUST_CLOUD, new DustCloudGenerator());
        generators.put(ParticleFieldType.ACCRETION_DISK, new AccretionDiskGenerator());
        // Linear generators
        generators.put(ParticleFieldType.RAIN, new RainGenerator());
        generators.put(ParticleFieldType.FIRE, new FireGenerator());
        generators.put(ParticleFieldType.EXPLOSION, new ExplosionGenerator());
        generators.put(ParticleFieldType.STARFIELD, new StarfieldGenerator());
        generators.put(ParticleFieldType.SWARM, new SwarmGenerator());
    }

    /**
     * Gets the appropriate generator for the given particle field type.
     */
    public static ParticleFieldGenerator getGenerator(ParticleFieldType type) {
        ParticleFieldGenerator generator = generators.get(type);
        if (generator == null) {
            throw new IllegalArgumentException("No generator registered for type: " + type);
        }
        return generator;
    }

    /**
     * Generates particle field elements using the appropriate generator.
     */
    public static List<ParticleFieldElement> generateElements(ParticleFieldConfiguration config, Random random) {
        return getGenerator(config.type()).generate(config, random);
    }

    /**
     * Generates particle field elements with a default random seed.
     */
    public static List<ParticleFieldElement> generateElements(ParticleFieldConfiguration config) {
        return generateElements(config, new Random(42));
    }

    // ========== ORBITAL PRESETS ==========

    /**
     * Saturn-like planetary ring: thin, dense, icy particles.
     */
    public static ParticleFieldConfiguration saturnRing() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.PLANETARY_RING)
                .innerRadius(15)
                .outerRadius(45)
                .numElements(10000)
                .minSize(0.2)
                .maxSize(0.8)
                .thickness(0.1)
                .maxInclinationDeg(0.5)
                .maxEccentricity(0.01)
                .baseAngularSpeed(0.004)
                .centralBodyRadius(10)
                .primaryColor(Color.rgb(230, 220, 200))
                .secondaryColor(Color.rgb(180, 170, 160))
                .name("Saturn-like Ring")
                .build();
    }

    /**
     * Uranus-like planetary ring: thin, dark, narrow bands.
     */
    public static ParticleFieldConfiguration uranusRing() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.PLANETARY_RING)
                .innerRadius(20)
                .outerRadius(35)
                .numElements(6000)
                .minSize(0.15)
                .maxSize(0.5)
                .thickness(0.05)
                .maxInclinationDeg(0.3)
                .maxEccentricity(0.008)
                .baseAngularSpeed(0.003)
                .centralBodyRadius(8)
                .primaryColor(Color.rgb(80, 80, 90))
                .secondaryColor(Color.rgb(50, 50, 60))
                .name("Uranus-like Ring")
                .build();
    }

    /**
     * Main asteroid belt: thick, sparse, rocky bodies.
     */
    public static ParticleFieldConfiguration mainAsteroidBelt() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.ASTEROID_BELT)
                .innerRadius(95)
                .outerRadius(105)
                .numElements(5000)
                .minSize(0.9)
                .maxSize(2.6)
                .thickness(8.0)
                .maxInclinationDeg(12.0)
                .maxEccentricity(0.06)
                .baseAngularSpeed(0.002)
                .centralBodyRadius(8)
                .primaryColor(Color.rgb(140, 130, 120))
                .secondaryColor(Color.rgb(100, 90, 80))
                .name("Asteroid Belt")
                .build();
    }

    /**
     * Kuiper belt: very wide, sparse, icy bodies.
     */
    public static ParticleFieldConfiguration kuiperBelt() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.ASTEROID_BELT)
                .innerRadius(150)
                .outerRadius(250)
                .numElements(3000)
                .minSize(1.0)
                .maxSize(3.5)
                .thickness(15.0)
                .maxInclinationDeg(20.0)
                .maxEccentricity(0.1)
                .baseAngularSpeed(0.0008)
                .centralBodyRadius(5)
                .primaryColor(Color.rgb(180, 190, 200))
                .secondaryColor(Color.rgb(140, 140, 150))
                .name("Kuiper Belt")
                .build();
    }

    /**
     * Protoplanetary debris disk: forming planetary system.
     */
    public static ParticleFieldConfiguration protoplanetaryDisk() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.DEBRIS_DISK)
                .innerRadius(10)
                .outerRadius(80)
                .numElements(8000)
                .minSize(0.3)
                .maxSize(1.5)
                .thickness(3.0)
                .maxInclinationDeg(5.0)
                .maxEccentricity(0.04)
                .baseAngularSpeed(0.003)
                .centralBodyRadius(6)
                .primaryColor(Color.rgb(200, 180, 150))
                .secondaryColor(Color.rgb(180, 140, 100))
                .name("Protoplanetary Disk")
                .build();
    }

    /**
     * Collision debris disk: aftermath of planetary collision.
     */
    public static ParticleFieldConfiguration collisionDebris() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.DEBRIS_DISK)
                .innerRadius(20)
                .outerRadius(50)
                .numElements(6000)
                .minSize(0.2)
                .maxSize(2.0)
                .thickness(5.0)
                .maxInclinationDeg(8.0)
                .maxEccentricity(0.08)
                .baseAngularSpeed(0.0025)
                .centralBodyRadius(7)
                .primaryColor(Color.rgb(160, 150, 140))
                .secondaryColor(Color.rgb(120, 100, 90))
                .name("Collision Debris")
                .build();
    }

    /**
     * Emission nebula: colorful, glowing gas cloud.
     */
    public static ParticleFieldConfiguration emissionNebula() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.DUST_CLOUD)
                .innerRadius(5)
                .outerRadius(100)
                .numElements(8000)
                .minSize(0.5)
                .maxSize(2.0)
                .thickness(60.0)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.02)
                .baseAngularSpeed(0.0003)
                .centralBodyRadius(4)
                .primaryColor(Color.rgb(255, 100, 150))
                .secondaryColor(Color.rgb(100, 200, 255))
                .name("Emission Nebula")
                .radialPower(0.4)
                .noiseStrength(0.4)
                .noiseOctaves(3)
                .seed(42L)
                .build();
    }

    /**
     * Dark nebula: obscuring dust cloud.
     */
    public static ParticleFieldConfiguration darkNebula() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.DUST_CLOUD)
                .innerRadius(10)
                .outerRadius(80)
                .numElements(5000)
                .minSize(0.8)
                .maxSize(2.5)
                .thickness(50.0)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.02)
                .baseAngularSpeed(0.0002)
                .centralBodyRadius(3)
                .primaryColor(Color.rgb(40, 35, 30))
                .secondaryColor(Color.rgb(20, 18, 15))
                .name("Dark Nebula")
                .radialPower(0.5)
                .noiseStrength(0.3)
                .noiseOctaves(3)
                .seed(43L)
                .build();
    }

    /**
     * Planetary nebula: expanding shell from dying star.
     */
    public static ParticleFieldConfiguration planetaryNebula() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.DUST_CLOUD)
                .innerRadius(15)
                .outerRadius(50)
                .numElements(8000)
                .minSize(0.3)
                .maxSize(1.2)
                .thickness(40.0)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.01)
                .baseAngularSpeed(0.0003)
                .centralBodyRadius(2)
                .primaryColor(Color.rgb(100, 255, 200))
                .secondaryColor(Color.rgb(200, 100, 255))
                .name("Planetary Nebula")
                .radialPower(0.7)
                .noiseStrength(0.25)
                .noiseOctaves(3)
                .seed(44L)
                .build();
    }

    /**
     * Supernova remnant: explosive debris with complex filaments.
     */
    public static ParticleFieldConfiguration supernovaRemnant() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.DUST_CLOUD)
                .innerRadius(20)
                .outerRadius(80)
                .numElements(10000)
                .minSize(0.4)
                .maxSize(1.8)
                .thickness(60.0)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.03)
                .baseAngularSpeed(0.0004)
                .centralBodyRadius(1)
                .primaryColor(Color.rgb(255, 150, 100))
                .secondaryColor(Color.rgb(255, 220, 100))
                .name("Supernova Remnant")
                .radialPower(0.65)
                .noiseStrength(0.6)
                .noiseOctaves(4)
                .seed(45L)
                .build();
    }

    /**
     * Reflection nebula: dust reflecting starlight (blue-shifted).
     */
    public static ParticleFieldConfiguration reflectionNebula() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.DUST_CLOUD)
                .innerRadius(5)
                .outerRadius(60)
                .numElements(6000)
                .minSize(0.3)
                .maxSize(1.5)
                .thickness(40.0)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.02)
                .baseAngularSpeed(0.00015)
                .centralBodyRadius(3)
                .primaryColor(Color.rgb(100, 150, 255))
                .secondaryColor(Color.rgb(150, 180, 255))
                .name("Reflection Nebula")
                .radialPower(0.35)
                .noiseStrength(0.2)
                .noiseOctaves(2)
                .seed(46L)
                .build();
    }

    /**
     * Black hole accretion disk: thin, hot, fast-rotating.
     */
    public static ParticleFieldConfiguration blackHoleAccretion() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.ACCRETION_DISK)
                .innerRadius(8)
                .outerRadius(50)
                .numElements(10000)
                .minSize(0.2)
                .maxSize(0.6)
                .thickness(0.5)
                .maxInclinationDeg(1.0)
                .maxEccentricity(0.01)
                .baseAngularSpeed(0.008)
                .centralBodyRadius(5)
                .primaryColor(Color.rgb(200, 220, 255))
                .secondaryColor(Color.rgb(255, 150, 50))
                .name("Black Hole Accretion Disk")
                .build();
    }

    /**
     * Neutron star accretion disk: very compact, extremely hot.
     */
    public static ParticleFieldConfiguration neutronStarAccretion() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.ACCRETION_DISK)
                .innerRadius(3)
                .outerRadius(25)
                .numElements(8000)
                .minSize(0.15)
                .maxSize(0.4)
                .thickness(0.3)
                .maxInclinationDeg(0.5)
                .maxEccentricity(0.005)
                .baseAngularSpeed(0.012)
                .centralBodyRadius(2)
                .primaryColor(Color.rgb(220, 240, 255))
                .secondaryColor(Color.rgb(255, 200, 100))
                .name("Neutron Star Accretion")
                .build();
    }

    // ========== LINEAR PRESETS ==========

    /**
     * Rainfall: blue/gray drops falling from the sky.
     */
    public static ParticleFieldConfiguration rainfall() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.RAIN)
                .numElements(3000)
                .minSize(0.1)
                .maxSize(0.3)
                .outerRadius(50)
                .primaryColor(Color.rgb(150, 180, 220))
                .secondaryColor(Color.rgb(100, 130, 170))
                .name("Rainfall")
                .gravity(new double[]{0, -9.8, 0})
                .wind(new double[]{1.0, 0, 0.5})
                .drag(0.02)
                .minLifetime(1.5)
                .maxLifetime(3.0)
                .minSpeed(5.0)
                .maxSpeed(12.0)
                .emitterSize(new double[]{100, 60, 100})
                .build();
    }

    /**
     * Campfire: rising fire particles with temperature colors.
     */
    public static ParticleFieldConfiguration campfire() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.FIRE)
                .numElements(2000)
                .minSize(0.2)
                .maxSize(0.8)
                .outerRadius(20)
                .primaryColor(Color.rgb(255, 255, 180))  // Yellow-white (hot)
                .secondaryColor(Color.rgb(180, 40, 10))   // Dark red (cool)
                .name("Campfire")
                .gravity(new double[]{0, -9.8, 0})
                .drag(0.3)
                .minLifetime(0.5)
                .maxLifetime(1.5)
                .minSpeed(3.0)
                .maxSpeed(8.0)
                .spreadAngle(25)
                .emitterSize(new double[]{3, 1, 3})
                .build();
    }

    /**
     * Explosion burst: radial particle burst with strong drag.
     */
    public static ParticleFieldConfiguration explosionBurst() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.EXPLOSION)
                .numElements(5000)
                .minSize(0.15)
                .maxSize(0.6)
                .outerRadius(40)
                .primaryColor(Color.rgb(255, 200, 50))   // Yellow-orange
                .secondaryColor(Color.rgb(150, 80, 30))  // Dark brown-orange
                .name("Explosion Burst")
                .gravity(new double[]{0, -3.0, 0})
                .drag(1.5)
                .minLifetime(0.5)
                .maxLifetime(2.0)
                .minSpeed(15.0)
                .maxSpeed(40.0)
                .build();
    }

    /**
     * Deep starfield: background star particles in spherical volume.
     */
    public static ParticleFieldConfiguration deepStarfield() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.STARFIELD)
                .numElements(5000)
                .minSize(0.1)
                .maxSize(0.5)
                .outerRadius(200)
                .primaryColor(Color.rgb(255, 255, 255))  // White
                .secondaryColor(Color.rgb(180, 200, 255)) // Blue-white
                .name("Deep Starfield")
                .minSpeed(0)
                .maxSpeed(0.01)
                .build();
    }

    /**
     * Firefly swarm: glowing particles with erratic movement.
     */
    public static ParticleFieldConfiguration fireflySwarm() {
        return ParticleFieldConfiguration.builder()
                .type(ParticleFieldType.SWARM)
                .numElements(500)
                .minSize(0.15)
                .maxSize(0.4)
                .outerRadius(30)
                .primaryColor(Color.rgb(200, 255, 100))  // Yellow-green glow
                .secondaryColor(Color.rgb(100, 200, 50))  // Green
                .name("Firefly Swarm")
                .drag(0.5)
                .minLifetime(3.0)
                .maxLifetime(8.0)
                .minSpeed(0.5)
                .maxSpeed(2.0)
                .build();
    }

    // ========== UTILITY METHODS ==========

    /**
     * Returns all available preset names.
     */
    public static String[] getPresetNames() {
        return new String[]{
                "Saturn Ring",
                "Uranus Ring",
                "Main Asteroid Belt",
                "Kuiper Belt",
                "Protoplanetary Disk",
                "Collision Debris",
                "Emission Nebula",
                "Dark Nebula",
                "Planetary Nebula",
                "Supernova Remnant",
                "Reflection Nebula",
                "Black Hole Accretion",
                "Neutron Star Accretion",
                "Rainfall",
                "Campfire",
                "Explosion Burst",
                "Deep Starfield",
                "Firefly Swarm"
        };
    }

    /**
     * Returns preset names for nebulae only.
     */
    public static String[] getNebulaPresetNames() {
        return new String[]{
                "Emission Nebula",
                "Dark Nebula",
                "Planetary Nebula",
                "Supernova Remnant",
                "Reflection Nebula"
        };
    }

    /**
     * Returns preset names for linear effects only.
     */
    public static String[] getLinearPresetNames() {
        return new String[]{
                "Rainfall",
                "Campfire",
                "Explosion Burst",
                "Deep Starfield",
                "Firefly Swarm"
        };
    }

    /**
     * Gets a preset configuration by name.
     */
    public static ParticleFieldConfiguration getPreset(String name) {
        return switch (name) {
            case "Saturn Ring" -> saturnRing();
            case "Uranus Ring" -> uranusRing();
            case "Main Asteroid Belt" -> mainAsteroidBelt();
            case "Kuiper Belt" -> kuiperBelt();
            case "Protoplanetary Disk" -> protoplanetaryDisk();
            case "Collision Debris" -> collisionDebris();
            case "Emission Nebula" -> emissionNebula();
            case "Dark Nebula" -> darkNebula();
            case "Planetary Nebula" -> planetaryNebula();
            case "Supernova Remnant" -> supernovaRemnant();
            case "Reflection Nebula" -> reflectionNebula();
            case "Black Hole Accretion" -> blackHoleAccretion();
            case "Neutron Star Accretion" -> neutronStarAccretion();
            case "Rainfall" -> rainfall();
            case "Campfire" -> campfire();
            case "Explosion Burst" -> explosionBurst();
            case "Deep Starfield" -> deepStarfield();
            case "Firefly Swarm" -> fireflySwarm();
            default -> throw new IllegalArgumentException("Unknown preset: " + name);
        };
    }
}
