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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.BoxShape;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;

class Ode4jRigidBodyWorldAdapterTest {

    @Test
    void supportsBodyLifecycleAndBulkRead() {
        Ode4jRigidBodyWorldAdapter adapter = new Ode4jRigidBodyWorldAdapter(new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0, -9.81, 0),
                1.0 / 60.0));

        PhysicsBodyHandle handle = adapter.createBody(new PhysicsBodyDefinition(
                PhysicsBodyType.DYNAMIC,
                1.0,
                new BoxShape(1, 1, 1),
                PhysicsBodyState.IDENTITY));
        adapter.step(0.25);

        Map<PhysicsBodyHandle, Ode4jRigidBodyWorldAdapter.TransformSnapshot> snapshots =
                adapter.readTransforms(List.of(handle));

        assertEquals(1, snapshots.size());
        assertTrue(snapshots.containsKey(handle));
        assertTrue(snapshots.get(handle).timestampSeconds() > 0.0);

        double[] positions = new double[3];
        double[] orientations = new double[4];
        int written = adapter.readTransforms(List.of(handle), positions, orientations);
        assertEquals(1, written);
        assertTrue(positions[1] < 0.0);
        assertEquals(1.0, orientations[3], 1e-9);
        assertThrows(IllegalArgumentException.class, () ->
                adapter.readTransforms(List.of(handle), new double[2], new double[4]));

        assertTrue(adapter.removeBody(handle));
        assertFalse(adapter.removeBody(handle));
        adapter.close();
    }
}
