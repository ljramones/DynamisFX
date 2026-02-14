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
import java.util.Optional;
import org.dynamisfx.physics.api.PhysicsBodyHandle;

/**
 * Tracks active rigid-body bindings for simulation objects inside physics zones.
 */
public final class ZoneBodyRegistry {

    private final java.util.Map<String, ZoneBodyBinding> bindingsByObjectId = new java.util.LinkedHashMap<>();

    public synchronized void bind(String objectId, ZoneId zoneId, PhysicsBodyHandle bodyHandle) {
        validateObjectId(objectId);
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        Objects.requireNonNull(bodyHandle, "bodyHandle must not be null");
        bindingsByObjectId.put(objectId, new ZoneBodyBinding(objectId, zoneId, bodyHandle));
    }

    public synchronized Optional<ZoneBodyBinding> bindingForObject(String objectId) {
        validateObjectId(objectId);
        return Optional.ofNullable(bindingsByObjectId.get(objectId));
    }

    public synchronized Optional<ZoneBodyBinding> unbind(String objectId) {
        validateObjectId(objectId);
        return Optional.ofNullable(bindingsByObjectId.remove(objectId));
    }

    private static void validateObjectId(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
    }

    public record ZoneBodyBinding(String objectId, ZoneId zoneId, PhysicsBodyHandle bodyHandle) {
        public ZoneBodyBinding {
            if (objectId == null || objectId.isBlank()) {
                throw new IllegalArgumentException("objectId must not be blank");
            }
            Objects.requireNonNull(zoneId, "zoneId must not be null");
            Objects.requireNonNull(bodyHandle, "bodyHandle must not be null");
        }
    }
}
