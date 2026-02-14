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

package org.dynamisfx.simulation.rigid;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Thread-safe in-memory buffer for rigid-body states keyed by object id.
 */
public final class RigidStateBuffer {

    private final Map<String, PhysicsBodyState> statesByObjectId = new ConcurrentHashMap<>();

    public void put(String objectId, PhysicsBodyState state) {
        validateObjectId(objectId);
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        statesByObjectId.put(objectId, state);
    }

    public Optional<PhysicsBodyState> get(String objectId) {
        validateObjectId(objectId);
        return Optional.ofNullable(statesByObjectId.get(objectId));
    }

    public boolean remove(String objectId) {
        validateObjectId(objectId);
        return statesByObjectId.remove(objectId) != null;
    }

    public Map<String, PhysicsBodyState> snapshot() {
        return Map.copyOf(statesByObjectId);
    }

    public boolean advanceLinear(String objectId, double dtSeconds, double timestampSeconds) {
        validateObjectId(objectId);
        if (!Double.isFinite(dtSeconds) || dtSeconds < 0.0) {
            throw new IllegalArgumentException("dtSeconds must be finite and >= 0");
        }
        if (!Double.isFinite(timestampSeconds)) {
            throw new IllegalArgumentException("timestampSeconds must be finite");
        }
        PhysicsBodyState current = statesByObjectId.get(objectId);
        if (current == null) {
            return false;
        }
        PhysicsVector3 p = current.position();
        PhysicsVector3 v = current.linearVelocity();
        PhysicsVector3 nextPosition = new PhysicsVector3(
                p.x() + (v.x() * dtSeconds),
                p.y() + (v.y() * dtSeconds),
                p.z() + (v.z() * dtSeconds));
        statesByObjectId.put(objectId, new PhysicsBodyState(
                nextPosition,
                current.orientation(),
                v,
                current.angularVelocity(),
                current.referenceFrame(),
                timestampSeconds));
        return true;
    }

    private static void validateObjectId(String objectId) {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId must not be blank");
        }
    }
}
