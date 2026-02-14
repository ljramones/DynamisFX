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

import java.util.Objects;
import java.util.function.Predicate;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

/**
 * Handles JavaFX pick results and routes them through a {@link SelectionModel3D}.
 */
public class PickSelectionHandler implements EventHandler<MouseEvent> {

    private final SelectionModel3D selectionModel;
    private final Predicate<Node> selectablePredicate;

    public PickSelectionHandler(SelectionModel3D selectionModel) {
        this(selectionModel, node -> true);
    }

    public PickSelectionHandler(SelectionModel3D selectionModel, Predicate<Node> selectablePredicate) {
        this.selectionModel = Objects.requireNonNull(selectionModel, "selectionModel must not be null");
        this.selectablePredicate = Objects.requireNonNull(selectablePredicate, "selectablePredicate must not be null");
    }

    @Override
    public void handle(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        final boolean extendSelection = event.isShiftDown() || event.isShortcutDown();
        applyPickResult(event.getPickResult(), extendSelection);
    }

    /**
     * Applies selection logic for a given pick result.
     *
     * @param pickResult the result to apply, may be null
     * @param extendSelection true to toggle in multi-select mode
     */
    public void applyPickResult(PickResult pickResult, boolean extendSelection) {
        Node target = resolveTarget(pickResult == null ? null : pickResult.getIntersectedNode());
        if (target == null) {
            if (selectionModel.isClearOnMiss()) {
                selectionModel.clearSelection();
            }
            return;
        }

        if (selectionModel.getSelectionMode() == SelectionModel3D.SelectionMode.MULTIPLE && extendSelection) {
            selectionModel.toggle(target);
        } else {
            selectionModel.select(target);
        }
    }

    public static PickSelectionHandler install(Node eventSource, SelectionModel3D model,
                                               Predicate<Node> selectablePredicate) {
        PickSelectionHandler handler = new PickSelectionHandler(model, selectablePredicate);
        eventSource.addEventHandler(MouseEvent.MOUSE_CLICKED, handler);
        return handler;
    }

    public void uninstall(Node eventSource) {
        eventSource.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
    }

    private Node resolveTarget(Node pickNode) {
        Node candidate = pickNode;
        while (candidate != null && !selectablePredicate.test(candidate)) {
            candidate = candidate.getParent();
        }
        return candidate;
    }
}

