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
 *
 */

package org.dynamisfx.samples.utilities;

import java.util.Objects;
import java.util.function.Predicate;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import org.dynamisfx.scene.selection.PickSelectionHandler;
import org.dynamisfx.scene.selection.SelectionModel3D;
import org.dynamisfx.scene.selection.TransformGizmo3D;

/**
 * Wires a selection model and transform gizmo into a 3D sub-scene.
 */
public final class SceneGizmoController {

    private final Group sceneRoot;
    private final SubScene subScene;
    private final SelectionModel3D selectionModel = new SelectionModel3D();
    private final TransformGizmo3D gizmo = new TransformGizmo3D();
    private PickSelectionHandler pickHandler;

    public SceneGizmoController(Group sceneRoot, SubScene subScene, Predicate<Node> selectablePredicate) {
        this.sceneRoot = Objects.requireNonNull(sceneRoot, "sceneRoot must not be null");
        this.subScene = Objects.requireNonNull(subScene, "subScene must not be null");

        Predicate<Node> resolvedPredicate = selectablePredicate == null ? node -> true : selectablePredicate;
        this.pickHandler = new PickSelectionHandler(selectionModel, resolvedPredicate);
        this.gizmo.bindToSelectionModel(selectionModel);
        this.sceneRoot.getChildren().add(gizmo);
        this.subScene.addEventHandler(MouseEvent.MOUSE_CLICKED, pickHandler);
    }

    public SelectionModel3D getSelectionModel() {
        return selectionModel;
    }

    public TransformGizmo3D getGizmo() {
        return gizmo;
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            subScene.removeEventHandler(MouseEvent.MOUSE_CLICKED, pickHandler);
            subScene.addEventHandler(MouseEvent.MOUSE_CLICKED, pickHandler);
        } else {
            subScene.removeEventHandler(MouseEvent.MOUSE_CLICKED, pickHandler);
            selectionModel.clearSelection();
        }
    }

    public void setMode(TransformGizmo3D.Mode mode) {
        gizmo.setMode(mode);
    }

    public void setSnapEnabled(boolean enabled) {
        gizmo.setSnapEnabled(enabled);
    }

    public void setSnapIncrements(double translationSnap, double rotationSnap, double scaleSnap) {
        gizmo.setTranslationSnapIncrement(translationSnap);
        gizmo.setRotationSnapIncrement(rotationSnap);
        gizmo.setScaleSnapIncrement(scaleSnap);
    }

    public void dispose() {
        subScene.removeEventHandler(MouseEvent.MOUSE_CLICKED, pickHandler);
        selectionModel.clearSelection();
        sceneRoot.getChildren().remove(gizmo);
        gizmo.unbindSelectionModel();
    }
}
