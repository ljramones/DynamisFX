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

import org.dynamisfx.physics.api.PhysicsBackend;
import org.dynamisfx.physics.api.PhysicsBackendFactory;

/**
 * Phase-2 kickoff factory for the ODE4j backend module.
 */
public final class Ode4jBackendFactory implements PhysicsBackendFactory {

    @Override
    public String backendId() {
        return "ode4j";
    }

    @Override
    public PhysicsBackend createBackend() {
        return new Ode4jBackend();
    }
}
