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

package org.dynamisfx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class ControlPanel extends VBox{

    public ControlPanel() {
        this.accordion = new ControlBasePane();
        VBox.setVgrow(accordion, Priority.ALWAYS);
        this.rootCategory = new ControlCategory("");
        this.accordion.getPanes().add(rootCategory);
        this.accordion.setExpandedPane(rootCategory);
        this.getChildren().add(accordion);
        this.setAlignment(Pos.CENTER);
    }
    
    private final ControlCategory rootCategory;
    private final ControlBasePane accordion;

    public ControlPanel(ControlCategory cat) {
        this();
        this.accordion.getPanes().clear();
        this.accordion.getPanes().add(cat);
        
    }

    public final TitledPane getExpandedPane() {
        return accordion.getExpandedPane();
    }

    public final void setExpandedPane(TitledPane value) {
        accordion.setExpandedPane(value);
    }

    public final ObjectProperty<TitledPane> expandedPaneProperty() {
        return accordion.expandedPaneProperty();
    }

    public final ObservableList<TitledPane> getPanes() {
        return accordion.getPanes();
        
    }
    
    public final void addToRoot(StackPane control){
        this.rootCategory.addControl(control);
    }
    
    public final void addToRoot(StackPane ... control){
        this.rootCategory.addControls(control);
    }
}
