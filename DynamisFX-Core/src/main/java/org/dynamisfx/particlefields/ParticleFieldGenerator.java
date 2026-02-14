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

import java.util.List;
import java.util.Random;

/**
 * Interface for generating particle field elements based on type-specific physics.
 * Each implementation encapsulates the generation logic for a specific particle field type.
 */
public interface ParticleFieldGenerator {

    /**
     * Generates all particle field elements based on the configuration.
     *
     * @param config the particle field configuration
     * @param random random number generator for reproducible results
     * @return list of generated particle field elements
     */
    List<ParticleFieldElement> generate(ParticleFieldConfiguration config, Random random);

    /**
     * Returns the particle field type this generator creates.
     */
    ParticleFieldType getFieldType();

    /**
     * Returns a description of this generator's behavior.
     */
    default String getDescription() {
        return getFieldType().getDisplayName() + " generator";
    }

    /**
     * Generates a single particle element. Used for respawning expired linear particles.
     *
     * @param config the particle field configuration
     * @param random random number generator
     * @return a single new particle field element
     */
    default ParticleFieldElement generateOne(ParticleFieldConfiguration config, Random random) {
        int origCount = config.numElements();
        ParticleFieldConfiguration singleConfig = config.withNumElements(1);
        List<ParticleFieldElement> result = generate(singleConfig, random);
        return result.isEmpty() ? null : result.get(0);
    }
}
