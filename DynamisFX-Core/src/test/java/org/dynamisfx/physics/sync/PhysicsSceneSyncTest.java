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

package org.dynamisfx.physics.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;

class PhysicsSceneSyncTest {

    @Test
    void appliesStatesToBoundNodes() {
        Map<PhysicsBodyHandle, PhysicsBodyState> states = new HashMap<>();
        PhysicsBodyHandle h1 = new PhysicsBodyHandle(1);
        PhysicsBodyHandle h2 = new PhysicsBodyHandle(2);
        states.put(h1, stateAt(10, 0, 0));
        states.put(h2, stateAt(0, 20, 0));

        FakeNode n1 = new FakeNode();
        FakeNode n2 = new FakeNode();
        PhysicsSceneSync<FakeNode> sync = new PhysicsSceneSync<>((node, state) -> node.state = state);
        sync.bind(h1, n1);
        sync.bind(h2, n2);

        sync.applyFrame(states::get);

        assertEquals(10.0, n1.state.position().x(), 1e-9);
        assertEquals(20.0, n2.state.position().y(), 1e-9);
        assertEquals(2, sync.bindingCount());
    }

    @Test
    void supportsUnbindOperations() {
        PhysicsBodyHandle handle = new PhysicsBodyHandle(7);
        FakeNode node = new FakeNode();
        PhysicsSceneSync<FakeNode> sync = new PhysicsSceneSync<>((n, s) -> n.state = s);
        sync.bind(handle, node);
        assertTrue(sync.unbindHandle(handle));
        assertFalse(sync.unbindHandle(handle));

        sync.bind(handle, node);
        assertTrue(sync.unbindNode(node));
        assertFalse(sync.unbindNode(node));
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(NullPointerException.class, () -> new PhysicsSceneSync<FakeNode>(null));
        PhysicsSceneSync<FakeNode> sync = new PhysicsSceneSync<>((n, s) -> {});
        assertThrows(IllegalArgumentException.class, () -> sync.bind(null, new FakeNode()));
        assertThrows(IllegalArgumentException.class, () -> sync.bind(new PhysicsBodyHandle(1), null));
        assertThrows(IllegalArgumentException.class, () -> sync.applyFrame(null));
    }

    private static PhysicsBodyState stateAt(double x, double y, double z) {
        return new PhysicsBodyState(
                new PhysicsVector3(x, y, z),
                PhysicsQuaternion.IDENTITY,
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                ReferenceFrame.WORLD,
                0.0);
    }

    private static final class FakeNode {
        private PhysicsBodyState state = PhysicsBodyState.IDENTITY;
    }
}
