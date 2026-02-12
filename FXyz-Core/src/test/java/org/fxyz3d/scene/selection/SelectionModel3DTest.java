package org.fxyz3d.scene.selection;

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

