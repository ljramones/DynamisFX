package org.dynamisfx.scene.selection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.PickResult;
import org.junit.jupiter.api.Test;

class PickSelectionHandlerTest {

    @Test
    void applyPickResultSelectsNearestAllowedAncestor() {
        SelectionModel3D model = new SelectionModel3D();
        Node allowed = new Group();
        Node child = new Group();
        ((Group) allowed).getChildren().add(child);

        PickSelectionHandler handler = new PickSelectionHandler(model, node -> node == allowed);
        handler.applyPickResult(new PickResult(child, 0.0, 0.0), false);

        assertTrue(model.isSelected(allowed));
        assertFalse(model.isSelected(child));
    }

    @Test
    void applyPickResultHonorsClearOnMissAndMultiToggle() {
        SelectionModel3D model = new SelectionModel3D();
        model.setSelectionMode(SelectionModel3D.SelectionMode.MULTIPLE);
        Node first = new Group();
        Node second = new Group();
        PickSelectionHandler handler = new PickSelectionHandler(model);

        handler.applyPickResult(new PickResult(first, 0.0, 0.0), false);
        handler.applyPickResult(new PickResult(second, 0.0, 0.0), true);
        assertEquals(2, model.getSelectedNodes().size());

        handler.applyPickResult(null, false);
        assertEquals(0, model.getSelectedNodes().size());

        model.select(first);
        model.setClearOnMiss(false);
        handler.applyPickResult(null, false);
        assertTrue(model.isSelected(first));
    }
}

