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

/**
 * Particle Field Rendering System.
 *
 * <p>This package provides a flexible system for rendering distributed particle structures
 * in 3D space. It supports two motion models:
 *
 * <h2>Orbital Motion</h2>
 * <p>Keplerian mechanics for astronomical structures:</p>
 * <ul>
 *   <li><b>Planetary Rings</b> - Saturn-like and Uranus-like ring systems</li>
 *   <li><b>Asteroid Belts</b> - Main belt, Kuiper belt structures</li>
 *   <li><b>Debris Disks</b> - Protoplanetary disks, collision remnants</li>
 *   <li><b>Dust Clouds</b> - Emission nebulae, dark nebulae, reflection nebulae</li>
 *   <li><b>Accretion Disks</b> - Black hole and neutron star accretion</li>
 * </ul>
 *
 * <h2>Linear Motion</h2>
 * <p>Velocity-based dynamics for environmental effects:</p>
 * <ul>
 *   <li><b>Rain</b> - Falling particles with gravity and wind</li>
 *   <li><b>Fire</b> - Rising particles with turbulence and temperature colors</li>
 *   <li><b>Explosions</b> - Radial bursts with drag deceleration</li>
 *   <li><b>Starfields</b> - Static/drifting background particles</li>
 *   <li><b>Swarms</b> - Erratic grouped particles</li>
 * </ul>
 *
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link org.dynamisfx.particlefields.ParticleFieldRenderer} - Main renderer for particle fields</li>
 *   <li>{@link org.dynamisfx.particlefields.ParticleFieldConfiguration} - Configuration record for field parameters</li>
 *   <li>{@link org.dynamisfx.particlefields.ParticleFieldFactory} - Factory with presets for common structures</li>
 *   <li>{@link org.dynamisfx.particlefields.ParticleFieldElement} - Individual particle with dual motion model</li>
 *   <li>{@link org.dynamisfx.particlefields.ParticleFieldGenerator} - Generator interface for creating particles</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create a Saturn-like ring
 * ParticleFieldConfiguration config = ParticleFieldFactory.saturnRing();
 * ParticleFieldRenderer renderer = new ParticleFieldRenderer(config, new Random(42));
 * parentGroup.getChildren().add(renderer.getGroup());
 *
 * // In animation loop:
 * renderer.update(timeScale);
 * renderer.updateMeshPositions();
 * }</pre>
 */
package org.dynamisfx.particlefields;
