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

package org.dynamisfx.physics.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PhysicsModelValidationTest {

    @Test
    void validatesShapesAndDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> new BoxShape(0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new SphereShape(-1));
        assertThrows(IllegalArgumentException.class, () -> new CapsuleShape(0, 1));

        PhysicsBodyState initial = PhysicsBodyState.IDENTITY;
        PhysicsBodyDefinition dynamic = new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                10.0,
                new SphereShape(1.0),
                initial);
        assertEquals(10.0, dynamic.massKg(), 1e-9);

        assertThrows(IllegalArgumentException.class, () -> new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC, 0.0, new SphereShape(1.0), initial));
    }

    @Test
    void validatesStateAndWorldConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new PhysicsVector3(1, 2, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new PhysicsQuaternion(0, 0, 0, Double.NaN));

        PhysicsBodyState state = new PhysicsBodyState(
                new PhysicsVector3(1, 2, 3),
                PhysicsQuaternion.IDENTITY,
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                ReferenceFrame.WORLD,
                12.0);
        assertEquals(12.0, state.timestampSeconds(), 1e-9);

        assertThrows(IllegalArgumentException.class, () -> new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                0.0));
        assertThrows(IllegalArgumentException.class, () -> new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                1.0 / 60.0,
                null));
        assertThrows(IllegalArgumentException.class, () -> new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                1.0 / 60.0,
                0,
                0.5,
                0.1,
                1e-5,
                0.1));
        assertThrows(IllegalArgumentException.class, () -> new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                1.0 / 60.0,
                8,
                -1.0,
                0.1,
                1e-5,
                0.1));
        assertThrows(IllegalArgumentException.class, () -> new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                1.0 / 60.0,
                8,
                0.5,
                1.5,
                1e-5,
                0.1));
    }
}
