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

package org.dynamisfx.physics.sync;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;

/**
 * Binds arbitrary node objects to physics handles and applies per-frame states.
 */
public final class PhysicsSceneSync<N> {

    private final NodeStateApplier<N> applier;
    private final Map<PhysicsBodyHandle, N> bindings = new LinkedHashMap<>();

    public PhysicsSceneSync(NodeStateApplier<N> applier) {
        this.applier = Objects.requireNonNull(applier, "applier must not be null");
    }

    public void bind(PhysicsBodyHandle handle, N node) {
        if (handle == null || node == null) {
            throw new IllegalArgumentException("handle and node must not be null");
        }
        bindings.put(handle, node);
    }

    public boolean unbindHandle(PhysicsBodyHandle handle) {
        if (handle == null) {
            return false;
        }
        return bindings.remove(handle) != null;
    }

    public boolean unbindNode(N node) {
        if (node == null) {
            return false;
        }
        PhysicsBodyHandle found = null;
        for (Map.Entry<PhysicsBodyHandle, N> entry : bindings.entrySet()) {
            if (entry.getValue() == node) {
                found = entry.getKey();
                break;
            }
        }
        return found != null && bindings.remove(found) != null;
    }

    public void clear() {
        bindings.clear();
    }

    public int bindingCount() {
        return bindings.size();
    }

    public void applyFrame(Function<PhysicsBodyHandle, PhysicsBodyState> stateProvider) {
        if (stateProvider == null) {
            throw new IllegalArgumentException("stateProvider must not be null");
        }
        for (Map.Entry<PhysicsBodyHandle, N> entry : bindings.entrySet()) {
            PhysicsBodyState state = stateProvider.apply(entry.getKey());
            if (state != null) {
                applier.apply(entry.getValue(), state);
            }
        }
    }
}
