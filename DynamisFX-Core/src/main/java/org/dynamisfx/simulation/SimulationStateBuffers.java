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

package org.dynamisfx.simulation;

import java.util.Objects;
import org.dynamisfx.simulation.orbital.OrbitalStateBuffer;
import org.dynamisfx.simulation.rigid.RigidStateBuffer;

/**
 * Aggregate holder for orbital and rigid state buffers used by simulation orchestration.
 */
public final class SimulationStateBuffers {

    private final OrbitalStateBuffer orbital;
    private final RigidStateBuffer rigid;

    public SimulationStateBuffers() {
        this(new OrbitalStateBuffer(), new RigidStateBuffer());
    }

    public SimulationStateBuffers(OrbitalStateBuffer orbital, RigidStateBuffer rigid) {
        this.orbital = Objects.requireNonNull(orbital, "orbital must not be null");
        this.rigid = Objects.requireNonNull(rigid, "rigid must not be null");
    }

    public OrbitalStateBuffer orbital() {
        return orbital;
    }

    public RigidStateBuffer rigid() {
        return rigid;
    }
}
