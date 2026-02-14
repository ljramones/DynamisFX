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

package org.dynamisfx.simulation.orbital;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory buffer for orbital states keyed by object id.
 */
public final class OrbitalStateBuffer {

    private final Map<String, OrbitalState> statesByObjectId = new ConcurrentHashMap<>();

    public void put(String objectId, OrbitalState state) {
        validateObjectId(objectId);
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        statesByObjectId.put(objectId, state);
    }

    public Optional<OrbitalState> get(String objectId) {
        validateObjectId(objectId);
        return Optional.ofNullable(statesByObjectId.get(objectId));
    }

    public boolean remove(String objectId) {
        validateObjectId(objectId);
        return statesByObjectId.remove(objectId) != null;
    }

    public Map<String, OrbitalState> snapshot() {
        return Map.copyOf(statesByObjectId);
    }

    private static void validateObjectId(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
    }
}
