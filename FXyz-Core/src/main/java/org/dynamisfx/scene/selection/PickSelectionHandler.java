/**
 * PickSelectionHandler.java
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

