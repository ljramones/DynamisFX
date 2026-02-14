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

package org.dynamisfx.physics.api;

import java.util.Collection;
import java.util.Optional;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Engine-neutral world surface used by FXyz runtime orchestration.
 */
public interface PhysicsWorld extends AutoCloseable {

    PhysicsCapabilities capabilities();

    PhysicsBodyHandle createBody(PhysicsBodyDefinition definition);

    boolean removeBody(PhysicsBodyHandle handle);

    Collection<PhysicsBodyHandle> bodies();

    PhysicsBodyState getBodyState(PhysicsBodyHandle handle);

    void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state);

    PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition);

    boolean removeConstraint(PhysicsConstraintHandle handle);

    Collection<PhysicsConstraintHandle> constraints();

    PhysicsRuntimeTuning runtimeTuning();

    void setRuntimeTuning(PhysicsRuntimeTuning tuning);

    default PhysicsVector3 gravity() {
        return PhysicsVector3.ZERO;
    }

    default void setGravity(PhysicsVector3 gravity) {
        // optional capability
    }

    default Optional<QueryCapability> queryCapability() {
        return Optional.empty();
    }

    default Optional<CcdCapability> ccdCapability() {
        return Optional.empty();
    }

    default Optional<MeshTerrainCapability> meshTerrainCapability() {
        return Optional.empty();
    }

    default Optional<ConstraintCapability> constraintCapability() {
        return Optional.empty();
    }

    void step(double dtSeconds);

    @Override
    default void close() {
        // no-op
    }
}
