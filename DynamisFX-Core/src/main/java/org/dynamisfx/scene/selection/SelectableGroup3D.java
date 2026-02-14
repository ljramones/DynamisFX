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
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

package org.dynamisfx.scene.selection;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;

/**
 * Group with built-in pick/selection handling.
 */
public class SelectableGroup3D extends Group {

    private final SelectionModel3D selectionModel = new SelectionModel3D();
    private final PickSelectionHandler selectionHandler;
    private final BooleanProperty selectionEnabled = new SimpleBooleanProperty(this, "selectionEnabled", true);

    public SelectableGroup3D() {
        super();
        selectionHandler = new PickSelectionHandler(selectionModel, this::isSelectablePickTarget);
        installSelectionHandler();
        selectionEnabled.addListener((obs, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                installSelectionHandler();
            } else {
                uninstallSelectionHandler();
            }
        });
    }

    public SelectionModel3D getSelectionModel() {
        return selectionModel;
    }

    public BooleanProperty selectionEnabledProperty() {
        return selectionEnabled;
    }

    public boolean isSelectionEnabled() {
        return selectionEnabled.get();
    }

    public void setSelectionEnabled(boolean enabled) {
        selectionEnabled.set(enabled);
    }

    /**
     * Override when you need custom selection filtering.
     */
    protected boolean isSelectablePickTarget(Node node) {
        return node != null
                && node != this
                && !(node instanceof Group)
                && !Boolean.TRUE.equals(node.getProperties().get(TransformGizmo3D.HANDLE_PROPERTY_KEY));
    }

    private void installSelectionHandler() {
        removeEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, selectionHandler);
        addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, selectionHandler);
    }

    private void uninstallSelectionHandler() {
        removeEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, selectionHandler);
    }
}
