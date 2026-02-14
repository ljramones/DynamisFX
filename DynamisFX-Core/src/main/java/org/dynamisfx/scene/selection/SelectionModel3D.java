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

package org.dynamisfx.scene.selection;

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

    public static final String SELECTED_PROPERTY_KEY = "org.dynamisfx.scene.selection.selected";

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

