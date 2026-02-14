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

package org.dynamisfx.physics.orekit;

import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;

/**
 * Orekit-oriented backend implementation.
 */
public final class OrekitBackend implements PhysicsBackend {

    private static final PhysicsCapabilities CAPABILITIES = new PhysicsCapabilities(
            false,
            true,
            false,
            false,
            false);

    @Override
    public String id() {
        return "orekit";
    }

    @Override
    public PhysicsCapabilities capabilities() {
        return CAPABILITIES;
    }

    @Override
    public PhysicsWorld createWorld(PhysicsWorldConfiguration configuration) {
        return new OrekitWorld(configuration);
    }
}
