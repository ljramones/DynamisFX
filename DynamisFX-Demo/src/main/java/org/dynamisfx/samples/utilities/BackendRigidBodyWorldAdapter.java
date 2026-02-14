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

package org.dynamisfx.samples.utilities;

import java.util.Collection;
import java.util.Objects;
import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;

/**
 * Generic adapter that exposes any rigid-body backend through {@link RigidBodyWorld}.
 */
final class BackendRigidBodyWorldAdapter implements RigidBodyWorld {

    private final PhysicsBackend backend;
    private final PhysicsWorld delegate;

    BackendRigidBodyWorldAdapter(PhysicsBackend backend, PhysicsWorldConfiguration configuration) {
        this.backend = Objects.requireNonNull(backend, "backend must not be null");
        this.delegate = backend.createWorld(Objects.requireNonNull(configuration, "configuration must not be null"));
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return delegate.capabilities();
    }

    @Override
    public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
        return delegate.createBody(definition);
    }

    @Override
    public boolean removeBody(PhysicsBodyHandle handle) {
        return delegate.removeBody(handle);
    }

    @Override
    public Collection<PhysicsBodyHandle> bodies() {
        return delegate.bodies();
    }

    @Override
    public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
        return delegate.getBodyState(handle);
    }

    @Override
    public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
        delegate.setBodyState(handle, state);
    }

    @Override
    public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
        return delegate.createConstraint(definition);
    }

    @Override
    public boolean removeConstraint(PhysicsConstraintHandle handle) {
        return delegate.removeConstraint(handle);
    }

    @Override
    public Collection<PhysicsConstraintHandle> constraints() {
        return delegate.constraints();
    }

    @Override
    public PhysicsRuntimeTuning runtimeTuning() {
        return delegate.runtimeTuning();
    }

    @Override
    public void setRuntimeTuning(PhysicsRuntimeTuning tuning) {
        delegate.setRuntimeTuning(tuning);
    }

    @Override
    public PhysicsVector3 gravity() {
        return delegate.gravity();
    }

    @Override
    public void setGravity(PhysicsVector3 gravity) {
        delegate.setGravity(gravity);
    }

    @Override
    public void step(double dtSeconds) {
        delegate.step(dtSeconds);
    }

    @Override
    public void close() {
        delegate.close();
        backend.close();
    }
}
