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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsWorld;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.physics.model.SphereShape;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class JoltNativeShimIntegrationTest {

    @Test
    void stepsBodyStateThroughNativeShimWhenLibraryPresent() {
        Path dylib = Path.of("native", "build", "libdynamisfx_jolt_cshim.dylib").toAbsolutePath().normalize();
        Path so = Path.of("native", "build", "libdynamisfx_jolt_cshim.so").toAbsolutePath().normalize();
        Path dll = Path.of("native", "build", "dynamisfx_jolt_cshim.dll").toAbsolutePath().normalize();
        Path libPath = Files.exists(dylib) ? dylib : (Files.exists(so) ? so : dll);
        Assumptions.assumeTrue(Files.exists(libPath), "Native shim binary not found under native/build");

        String oldPath = System.getProperty(JoltNativeBridge.NATIVE_PATH_PROPERTY);
        System.setProperty(JoltNativeBridge.NATIVE_PATH_PROPERTY, libPath.toString());
        try {
            JoltNativeBridge bridge = new JoltNativeBridge();
            assertEquals(JoltNativeBridge.BACKEND_MODE_STUB, bridge.backendMode());
            JoltBackend backend = new JoltBackend(bridge);
            PhysicsWorld world = backend.createWorld(new PhysicsWorldConfiguration(
                    ReferenceFrame.WORLD,
                    new PhysicsVector3(0.0, -9.81, 0.0),
                    1.0 / 60.0));
            PhysicsBodyHandle body = world.createBody(new PhysicsBodyDefinition(
                    PhysicsBodyType.DYNAMIC,
                    1.0,
                    new SphereShape(0.5),
                    new PhysicsBodyState(
                            PhysicsVector3.ZERO,
                            PhysicsQuaternion.IDENTITY,
                            PhysicsVector3.ZERO,
                            PhysicsVector3.ZERO,
                            ReferenceFrame.WORLD,
                            0.0)));
            assertNotNull(body);
            assertTrue(world.bodies().contains(body));

            world.step(0.5);
            PhysicsBodyState state = world.getBodyState(body);
            assertEquals(-4.905, state.linearVelocity().y(), 1e-6);
            assertEquals(-2.4525, state.position().y(), 1e-6);
            world.close();
        } finally {
            if (oldPath == null) {
                System.clearProperty(JoltNativeBridge.NATIVE_PATH_PROPERTY);
            } else {
                System.setProperty(JoltNativeBridge.NATIVE_PATH_PROPERTY, oldPath);
            }
        }
    }
}
