/**
 * SceneGizmoController.java
 *
 * Copyright (c) 2013-2026, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
