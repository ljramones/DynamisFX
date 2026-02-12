/**
 * SelectionModel3D.java
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

package org.fxyz3d.scene.selection;

import java.util.LinkedHashSet;
import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.scene.Node;

/**
 * Generic selection model for JavaFX 3D nodes.
 */
public class SelectionModel3D {

    public enum SelectionMode {
        SINGLE,
        MULTIPLE
    }

    public static final String SELECTED_PROPERTY_KEY = "org.fxyz3d.scene.selection.selected";

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    private final ObservableSet<Node> selectedNodes = FXCollections.observableSet(new LinkedHashSet<>());
    private final ObservableSet<Node> readOnlySelectedNodes = FXCollections.unmodifiableObservableSet(selectedNodes);
    private final ObjectProperty<SelectionMode> selectionMode = new SimpleObjectProperty<>(SelectionMode.SINGLE);
    private final BooleanProperty clearOnMiss = new SimpleBooleanProperty(true);

    public ObservableSet<Node> getSelectedNodes() {
        return readOnlySelectedNodes;
    }

    public ObjectProperty<SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    public SelectionMode getSelectionMode() {
        return selectionMode.get();
    }

    public void setSelectionMode(SelectionMode mode) {
        selectionMode.set(mode);
    }

    public BooleanProperty clearOnMissProperty() {
        return clearOnMiss;
    }

    public boolean isClearOnMiss() {
        return clearOnMiss.get();
    }

    public void setClearOnMiss(boolean value) {
        clearOnMiss.set(value);
    }

    public Optional<Node> getPrimarySelection() {
        return selectedNodes.stream().findFirst();
    }

    public boolean isSelected(Node node) {
        return node != null && selectedNodes.contains(node);
    }

    public void select(Node node) {
        if (node == null) {
            clearSelection();
            return;
        }

        if (getSelectionMode() == SelectionMode.SINGLE) {
            clearSelection();
            selectedNodes.add(node);
            updateSelectionState(node, true);
            return;
        }

        if (!selectedNodes.contains(node)) {
            selectedNodes.add(node);
            updateSelectionState(node, true);
        }
    }

    public void toggle(Node node) {
        if (node == null) {
            return;
        }

        if (selectedNodes.remove(node)) {
            updateSelectionState(node, false);
        } else {
            if (getSelectionMode() == SelectionMode.SINGLE) {
                clearSelection();
            }
            selectedNodes.add(node);
            updateSelectionState(node, true);
        }
    }

    public void clearSelection() {
        if (selectedNodes.isEmpty()) {
            return;
        }
        selectedNodes.forEach(n -> updateSelectionState(n, false));
        selectedNodes.clear();
    }

    private static void updateSelectionState(Node node, boolean selected) {
        node.getProperties().put(SELECTED_PROPERTY_KEY, selected);
        node.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
    }
}

