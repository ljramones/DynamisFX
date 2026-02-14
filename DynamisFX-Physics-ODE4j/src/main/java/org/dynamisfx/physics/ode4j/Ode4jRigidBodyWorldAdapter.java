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

package org.dynamisfx.physics.ode4j;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
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
 * Adapter shell that exposes the ODE4j world through the {@link RigidBodyWorld} contract.
 */
public final class Ode4jRigidBodyWorldAdapter implements RigidBodyWorld {

    private final PhysicsBackend backend;
    private final PhysicsWorld delegate;

    public Ode4jRigidBodyWorldAdapter(PhysicsWorldConfiguration configuration) {
        backend = new Ode4jBackend();
        delegate = backend.createWorld(configuration);
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

    /**
     * Bulk-read convenience API for transform snapshot extraction.
     */
    public Map<PhysicsBodyHandle, TransformSnapshot> readTransforms(Collection<PhysicsBodyHandle> handles) {
        Objects.requireNonNull(handles, "handles must not be null");
        Map<PhysicsBodyHandle, TransformSnapshot> result = new LinkedHashMap<>();
        for (PhysicsBodyHandle handle : handles) {
            PhysicsBodyState state = delegate.getBodyState(handle);
            result.put(handle, new TransformSnapshot(
                    state.position().x(),
                    state.position().y(),
                    state.position().z(),
                    state.orientation().x(),
                    state.orientation().y(),
                    state.orientation().z(),
                    state.orientation().w(),
                    state.timestampSeconds()));
        }
        return result;
    }

    /**
     * Writes transform snapshots into flat arrays to avoid per-frame map allocations.
     *
     * @return number of written body transforms
     */
    public int readTransforms(
            Collection<PhysicsBodyHandle> handles,
            double[] positions,
            double[] orientations) {
        Objects.requireNonNull(handles, "handles must not be null");
        Objects.requireNonNull(positions, "positions must not be null");
        Objects.requireNonNull(orientations, "orientations must not be null");
        int count = handles.size();
        if (positions.length < count * 3) {
            throw new IllegalArgumentException("positions buffer too small");
        }
        if (orientations.length < count * 4) {
            throw new IllegalArgumentException("orientations buffer too small");
        }
        int i = 0;
        for (PhysicsBodyHandle handle : handles) {
            PhysicsBodyState state = delegate.getBodyState(handle);
            int p = i * 3;
            positions[p] = state.position().x();
            positions[p + 1] = state.position().y();
            positions[p + 2] = state.position().z();
            int q = i * 4;
            orientations[q] = state.orientation().x();
            orientations[q + 1] = state.orientation().y();
            orientations[q + 2] = state.orientation().z();
            orientations[q + 3] = state.orientation().w();
            i++;
        }
        return count;
    }

    @Override
    public void close() {
        delegate.close();
        backend.close();
    }

    public record TransformSnapshot(
            double posX,
            double posY,
            double posZ,
            double quatX,
            double quatY,
            double quatZ,
            double quatW,
            double timestampSeconds) {
    }
}
