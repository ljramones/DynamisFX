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

package org.dynamisfx.simulation.coupling;

import java.util.Collection;
import java.util.Optional;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Coordinates object ownership across orbital and zone-local rigid simulations.
 */
public interface CouplingManager {

    void registerZone(PhysicsZone zone);

    boolean removeZone(ZoneId zoneId);

    Collection<PhysicsZone> zones();

    Optional<ObjectSimulationMode> modeFor(String objectId);

    void setMode(String objectId, ObjectSimulationMode mode);

    void update(double simulationTimeSeconds);
}
