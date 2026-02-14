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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.Group;
import javafx.scene.Node;
import org.junit.jupiter.api.Test;

class SelectionModel3DTest {

    @Test
    void singleSelectionReplacesCurrentSelection() {
        SelectionModel3D model = new SelectionModel3D();
        Node first = new Group();
        Node second = new Group();

        model.select(first);
        model.select(second);

        assertEquals(1, model.getSelectedNodes().size());
        assertFalse(model.isSelected(first));
        assertTrue(model.isSelected(second));
        assertEquals(Boolean.FALSE, first.getProperties().get(SelectionModel3D.SELECTED_PROPERTY_KEY));
        assertEquals(Boolean.TRUE, second.getProperties().get(SelectionModel3D.SELECTED_PROPERTY_KEY));
    }

    @Test
    void multiSelectionToggleAddsAndRemovesNodes() {
        SelectionModel3D model = new SelectionModel3D();
        model.setSelectionMode(SelectionModel3D.SelectionMode.MULTIPLE);
        Node first = new Group();
        Node second = new Group();

        model.select(first);
        model.toggle(second);
        model.toggle(first);

        assertEquals(1, model.getSelectedNodes().size());
        assertFalse(model.isSelected(first));
        assertTrue(model.isSelected(second));
        assertEquals(Boolean.FALSE, first.getProperties().get(SelectionModel3D.SELECTED_PROPERTY_KEY));
        assertEquals(Boolean.TRUE, second.getProperties().get(SelectionModel3D.SELECTED_PROPERTY_KEY));
    }
}

