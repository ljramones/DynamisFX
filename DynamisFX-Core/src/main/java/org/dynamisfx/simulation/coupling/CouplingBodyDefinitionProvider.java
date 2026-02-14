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

import java.util.Objects;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.SphereShape;

/**
 * Supplies rigid-body definitions for objects promoted into physics-active mode.
 */
@FunctionalInterface
public interface CouplingBodyDefinitionProvider {

    PhysicsBodyDefinition createBodyDefinition(
            String objectId,
            CouplingModeTransitionEvent event,
            PhysicsZone zone,
            PhysicsBodyState seedState);

    static CouplingBodyDefinitionProvider dynamicSphere(double radiusMeters, double massKg) {
        if (!(radiusMeters > 0.0)) {
            throw new IllegalArgumentException("radiusMeters must be > 0");
        }
        if (!(massKg > 0.0) || !Double.isFinite(massKg)) {
            throw new IllegalArgumentException("massKg must be finite and > 0");
        }
        return (objectId, event, zone, seedState) -> {
            if (objectId == null || objectId.isBlank()) {
                throw new IllegalArgumentException("objectId must not be blank");
            }
            Objects.requireNonNull(event, "event must not be null");
            Objects.requireNonNull(zone, "zone must not be null");
            Objects.requireNonNull(seedState, "seedState must not be null");
            return new PhysicsBodyDefinition(
                    PhysicsBodyType.DYNAMIC,
                    massKg,
                    new SphereShape(radiusMeters),
                    seedState);
        };
    }
}
