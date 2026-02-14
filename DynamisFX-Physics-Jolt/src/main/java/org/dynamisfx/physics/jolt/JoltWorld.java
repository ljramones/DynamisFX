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

package org.dynamisfx.physics.jolt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

/**
 * Initial Jolt world shell that fails fast until the native c-shim is present.
 */
public final class JoltWorld implements PhysicsWorld {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            true,
            false,
            true,
            true,
            true);

    private final PhysicsWorldConfiguration configuration;
    private final JoltNativeBridge bridge;
    private final Set<PhysicsBodyHandle> bodyHandles = new LinkedHashSet<>();
    private long worldHandle;
    private double simulationTimeSeconds;
    private boolean closed;

    JoltWorld(PhysicsWorldConfiguration configuration, JoltNativeBridge bridge) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.bridge = Objects.requireNonNull(bridge, "bridge must not be null");
        requireNative();
        this.worldHandle = bridge.worldCreate(configuration);
        if (worldHandle == 0L) {
            throw unavailable("worldCreate");
        }
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
        Objects.requireNonNull(definition, "definition must not be null");
        ensureOpen();
        long nativeBody = bridge.bodyCreate(worldHandle, definition);
        if (nativeBody == 0L) {
            throw unavailable("createBody");
        }
        PhysicsBodyHandle handle = new PhysicsBodyHandle(nativeBody);
        bodyHandles.add(handle);
        return handle;
    }

    @Override
    public boolean removeBody(PhysicsBodyHandle handle) {
        Objects.requireNonNull(handle, "handle must not be null");
        ensureOpen();
        if (!bodyHandles.contains(handle)) {
            return false;
        }
        int status = bridge.bodyDestroy(worldHandle, handle.value());
        if (status != 0) {
            throw unavailable("removeBody");
        }
        return bodyHandles.remove(handle);
    }

    @Override
    public Collection<PhysicsBodyHandle> bodies() {
        ensureOpen();
        return List.copyOf(bodyHandles);
    }

    @Override
    public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
        Objects.requireNonNull(handle, "handle must not be null");
        ensureOpen();
        PhysicsBodyState state = bridge.bodyGetState(
                worldHandle,
                handle.value(),
                configuration.referenceFrame(),
                simulationTimeSeconds);
        if (state == null) {
            throw unavailable("getBodyState");
        }
        return state;
    }

    @Override
    public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
        Objects.requireNonNull(handle, "handle must not be null");
        Objects.requireNonNull(state, "state must not be null");
        ensureOpen();
        int status = bridge.bodySetState(worldHandle, handle.value(), state);
        if (status != 0) {
            throw unavailable("setBodyState");
        }
    }

    @Override
    public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
        throw unavailable("createConstraint");
    }

    @Override
    public boolean removeConstraint(PhysicsConstraintHandle handle) {
        throw unavailable("removeConstraint");
    }

    @Override
    public Collection<PhysicsConstraintHandle> constraints() {
        return List.of();
    }

    @Override
    public PhysicsRuntimeTuning runtimeTuning() {
        return configuration.runtimeTuning();
    }

    @Override
    public void setRuntimeTuning(PhysicsRuntimeTuning tuning) {
        throw unavailable("setRuntimeTuning");
    }

    @Override
    public PhysicsVector3 gravity() {
        return configuration.gravity();
    }

    @Override
    public void setGravity(PhysicsVector3 gravity) {
        throw unavailable("setGravity");
    }

    @Override
    public void step(double dtSeconds) {
        ensureOpen();
        if (!(dtSeconds > 0.0) || !Double.isFinite(dtSeconds)) {
            throw new IllegalArgumentException("dtSeconds must be > 0 and finite");
        }
        int status = bridge.worldStep(worldHandle, dtSeconds);
        if (status != 0) {
            throw unavailable("step");
        }
        simulationTimeSeconds += dtSeconds;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (worldHandle != 0L) {
            bridge.worldDestroy(worldHandle);
            worldHandle = 0L;
        }
        bodyHandles.clear();
    }

    private void requireNative() {
        if (!bridge.isAvailable()) {
            throw unavailable("initialize");
        }
        if (bridge.apiVersion() != JoltNativeBridge.API_VERSION) {
            throw unavailable("apiVersion");
        }
    }

    private void ensureOpen() {
        if (closed || worldHandle == 0L) {
            throw new IllegalStateException("Jolt world is closed");
        }
    }

    private IllegalStateException unavailable(String operation) {
        return new IllegalStateException(
                "Jolt native backend unavailable for operation "
                        + operation
                        + "; expected native library "
                        + JoltNativeBridge.LIBRARY_NAME
                        + " ("
                        + JoltNativeBridge.expectedLibraryFileName()
                        + "), load-status="
                        + bridge.loadDescription()
                        + ", configured path property="
                        + JoltNativeBridge.NATIVE_PATH_PROPERTY
                        + ", env="
                        + JoltNativeBridge.NATIVE_PATH_ENV);
    }
}
