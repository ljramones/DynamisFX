/**
 * SelectableGroup3D.java
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
