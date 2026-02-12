package org.fxyz3d.scene.selection;

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

