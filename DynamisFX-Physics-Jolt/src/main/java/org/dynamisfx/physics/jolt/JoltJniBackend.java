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

import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;

/**
 * Jolt backend backed by the published jolt-jni bindings.
 */
final class JoltJniBackend implements PhysicsBackend {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            true,
            false,
            true,
            true,
            true);

    private final JoltJniNativeLoader.LoadResult loadResult;

    JoltJniBackend() {
        this.loadResult = JoltJniNativeLoader.ensureLoaded();
    }

    boolean isAvailable() {
        return loadResult.available();
    }

    @Override
    public String id() {
        return "jolt";
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public PhysicsWorld createWorld(PhysicsWorldConfiguration configuration) {
        if (!loadResult.available()) {
            throw unavailable("initialize");
        }
        return new JoltJniWorld(configuration, loadResult.description());
    }

    private IllegalStateException unavailable(String operation) {
        return new IllegalStateException(
                "jolt-jni backend unavailable for operation "
                        + operation
                        + "; load-status="
                        + loadResult.description()
                        + ", path property="
                        + JoltJniNativeLoader.NATIVE_PATH_PROPERTY
                        + ", env="
                        + JoltJniNativeLoader.NATIVE_PATH_ENV);
    }
}
