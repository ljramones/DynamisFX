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

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.dynamisfx.scene.CameraView;

/**
 * FXML Controller class
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class CameraViewControl extends ControlBase<BooleanProperty>{
    @FXML
    private Label label;
    @FXML
    private CheckBox controlsEnabled;
    @FXML
    private VBox container;
    
    private final CameraView view;

    public CameraViewControl(BooleanProperty enabled, SubScene subScene, StackPane parentPane) {
        super("/org/dynamisfx/controls/CameraViewControl.fxml", enabled);
        
        this.view = new CameraView(subScene);
        this.view.setFitWidth(200);
        this.view.setFitHeight(150);
        this.view.setSmooth(true);
        
        this.controlsEnabled.selectedProperty().addListener(l->{
            if(controlsEnabled.isSelected()){
                view.setFirstPersonNavigationEabled(true);
            }else if(!controlsEnabled.isSelected()){
                view.setFirstPersonNavigationEabled(false);
            }
        });
        
        container.getChildren().add(1, view);
        
        final ComboBox<Pos> positions = new ComboBox<>();
        positions.getItems().addAll(Pos.TOP_LEFT, Pos.TOP_RIGHT, Pos.BOTTOM_LEFT, Pos.BOTTOM_RIGHT);
        positions.getSelectionModel().selectLast();
        positions.valueProperty().addListener(l->{
            switch(positions.getValue()){
                case TOP_LEFT:
                    StackPane.setAlignment(CameraViewControl.this, Pos.TOP_LEFT);
                    break;
                case TOP_RIGHT:
                    StackPane.setAlignment(CameraViewControl.this, Pos.TOP_RIGHT);
                    break;
                case BOTTOM_LEFT:
                    StackPane.setAlignment(CameraViewControl.this, Pos.BOTTOM_LEFT);
                    break;
                case BOTTOM_RIGHT:
                    StackPane.setAlignment(CameraViewControl.this, Pos.BOTTOM_RIGHT);
                    break;
            }
        });
        positions.setPrefSize(USE_COMPUTED_SIZE, USE_PREF_SIZE);
        container.getChildren().add(positions);
        
        parentPane.getChildren().add(CameraViewControl.this);
        
        enabled.addListener(l->{
            if(enabled.getValue()){
                view.startViewing();
            }else{
                
            }
        });
        StackPane.setMargin(CameraViewControl.this, new Insets(120));
    }

       
    
}
