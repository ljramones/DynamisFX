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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dynamisfx.physics.model.PhysicsVector3;
import org.junit.jupiter.api.Test;

class PhysicsConstraintDefinitionTest {

    @Test
    void validatesConstraintDefinition() {
        PhysicsConstraintDefinition definition = new PhysicsConstraintDefinition(
                PhysicsConstraintType.BALL,
                new PhysicsBodyHandle(1),
                new PhysicsBodyHandle(2),
                new PhysicsVector3(0, 1, 2));
        assertEquals(PhysicsConstraintType.BALL, definition.type());

        assertThrows(IllegalArgumentException.class, () ->
                new PhysicsConstraintDefinition(null, new PhysicsBodyHandle(1), new PhysicsBodyHandle(2), null));
        assertThrows(IllegalArgumentException.class, () ->
                new PhysicsConstraintDefinition(PhysicsConstraintType.FIXED, null, new PhysicsBodyHandle(2), null));
        assertThrows(IllegalArgumentException.class, () ->
                new PhysicsConstraintDefinition(
                        PhysicsConstraintType.HINGE,
                        new PhysicsBodyHandle(1),
                        new PhysicsBodyHandle(2),
                        null,
                        null,
                        null,
                        null));
        PhysicsConstraintDefinition slider = new PhysicsConstraintDefinition(
                PhysicsConstraintType.SLIDER,
                new PhysicsBodyHandle(1),
                new PhysicsBodyHandle(2),
                null,
                new PhysicsVector3(1, 0, 0),
                -1.0,
                1.0);
        assertEquals(PhysicsConstraintType.SLIDER, slider.type());
        assertThrows(IllegalArgumentException.class, () ->
                new PhysicsConstraintDefinition(
                        PhysicsConstraintType.SLIDER,
                        new PhysicsBodyHandle(1),
                        new PhysicsBodyHandle(2),
                        null,
                        new PhysicsVector3(1, 0, 0),
                        2.0,
                        1.0));
    }
}
