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

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class ControlCategory  extends TitledPane{
    
    @FXML
    private ListView<StackPane> listView;
    
    private final ObservableList<StackPane> controlItems;

    private ControlCategory() {
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dynamisfx/controls/ControlPanelTitlePane.fxml"));
            loader.setRoot(ControlCategory.this);
            loader.setController(ControlCategory.this);
            loader.load();
        } catch (IOException ex) {            
            Logger.getLogger(ControlCategory.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.controlItems = FXCollections.observableArrayList();
        this.listView.setItems(controlItems);
        
        //this.getStyleClass().add("fxyz3d-control-category");
    }    
    
    public ControlCategory(String title) {
        this();
        this.setText(title);
        this.setFocusTraversable(false);
        //EasyBind.listBind(controlItems, controls.getChildren());       
        this.listView.setCellFactory(p -> new ListCell<>() {
                {
                    this.setFocusTraversable(false);
                    this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }

                @Override
                protected void updateItem(StackPane item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(item != null && !empty ? item : null);
                }

            });
    }
        
    public void addControl(StackPane n){
        if(!controlItems.contains(n)){
            controlItems.add(n);
        }
    }
    public void addControls(StackPane ... ctrls){
        if(!controlItems.containsAll(Arrays.asList(ctrls))){
            controlItems.addAll(ctrls);
        }
    }

    public void removeControl(Node ...  n){
        controlItems.removeAll(Arrays.asList(n));
    }
    
    public void removeIf(Predicate<StackPane> filter) {
        controlItems.removeIf(filter);
    }
    
}
