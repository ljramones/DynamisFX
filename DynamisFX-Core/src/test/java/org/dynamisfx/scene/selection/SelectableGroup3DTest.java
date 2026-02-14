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

package org.dynamisfx.scene.selection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Box;
import org.junit.jupiter.api.Test;

class SelectableGroup3DTest {

    @Test
    void selectableTargetFilterExcludesGroupsAndGizmoHandles() {
        ExposedSelectableGroup3D group = new ExposedSelectableGroup3D();
        Node regularNode = new Box(1, 1, 1);
        Node nestedGroup = new Group();
        Node gizmoHandle = new Box(1, 1, 1);
        gizmoHandle.getProperties().put(TransformGizmo3D.HANDLE_PROPERTY_KEY, Boolean.TRUE);

        assertTrue(group.canSelect(regularNode));
        assertFalse(group.canSelect(nestedGroup));
        assertFalse(group.canSelect(gizmoHandle));
    }

    private static final class ExposedSelectableGroup3D extends SelectableGroup3D {
        boolean canSelect(Node node) {
            return isSelectablePickTarget(node);
        }
    }
}

