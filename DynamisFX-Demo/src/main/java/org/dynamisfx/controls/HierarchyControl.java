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

package org.dynamisfx.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;

/**
 * FXML Controller class
 *
 * @author Jose Pereda
 */
public class HierarchyControl extends ControlBase<Property<Node>> {

    @FXML private TreeTableView<Node> hierarchyTreeTable;
    @FXML private TreeTableColumn<Node, String> nodeColumn;
    @FXML private TreeTableColumn<Node, String> idColumn;
    @FXML private TreeTableColumn<Node, Boolean> visibilityColumn;

    public HierarchyControl(Property<Node> prop) {
        super("/org/dynamisfx/controls/HierarchyControl.fxml", prop);
        nodeColumn.setCellValueFactory(p -> p.getValue().valueProperty().asString());
        idColumn.setCellValueFactory(p -> p.getValue().getValue().idProperty());
        visibilityColumn.setCellValueFactory(p -> p.getValue().getValue().visibleProperty());
        visibilityColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(visibilityColumn));

        loadBindings();
    }

    public final void loadBindings() {
        if (controlledProperty == null) {
            return;
        }
        hierarchyTreeTable.rootProperty().bind(Bindings.createObjectBinding(() -> {
                Node content3D = controlledProperty.getValue();
                return (content3D != null) ? new HierarchyTreeItem(content3D) : null;
            }, controlledProperty));
    }

    private class HierarchyTreeItem extends TreeItem<Node> {

        public HierarchyTreeItem(Node node) {
            super(node);
            if (node instanceof Parent) {
                for (Node n : ((Parent) node).getChildrenUnmodifiable()) {
                    getChildren().add(new HierarchyTreeItem(n));
                }
            }
            node.setOnMouseClicked(t -> {
                TreeItem<Node> parent = getParent();
                while (parent != null) {
                    parent.setExpanded(true);
                    parent = parent.getParent();
                }
                hierarchyTreeTable.getSelectionModel().select(HierarchyTreeItem.this);
                hierarchyTreeTable.scrollTo(hierarchyTreeTable.getSelectionModel().getSelectedIndex());
                t.consume();
            });
        }
    }
}
