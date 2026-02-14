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
import java.util.OptionalDouble;

/**
 * Supplies transition-relevant observations for coupling policies.
 */
public interface CouplingObservationProvider {

    /**
     * Returns distance (meters) from object to nearest relevant zone boundary/anchor metric.
     * Implementations define how distance is measured.
     */
    OptionalDouble distanceMetersToNearestZone(String objectId, Collection<PhysicsZone> zones);

    /**
     * Optional time-to-intercept estimate in seconds for pre-contact promotion.
     */
    OptionalDouble predictedInterceptSeconds(String objectId, Collection<PhysicsZone> zones);

    /**
     * Indicates whether the object currently has active physical contact/constraints.
     */
    boolean hasActiveContact(String objectId);

    /**
     * Optional estimated altitude above local surface/terrain in meters.
     */
    default OptionalDouble altitudeMetersAboveSurface(String objectId, Collection<PhysicsZone> zones) {
        return OptionalDouble.empty();
    }
}
