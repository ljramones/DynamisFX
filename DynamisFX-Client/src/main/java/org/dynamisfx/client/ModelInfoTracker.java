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

package org.dynamisfx.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.HiddenSidesPane;
import org.dynamisfx.ExtrasAndTests.CustomWindow;

/**
 * FXML Controller class
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class ModelInfoTracker extends StackPane {
    @FXML
    private HBox headerArea;
    @FXML
    private Label sampleTitle;
    @FXML
    private Button hideStatus;
    @FXML
    private HBox content;
    @FXML
    private Label nodeCount;
    @FXML
    private Label timeToBuild;
    @FXML
    private Label width;
    @FXML
    private Label height;
    @FXML
    private Label depth;
    @FXML
    private Label points;
    @FXML
    private Label faces;

    private HiddenSidesPane parentPane;
    public ModelInfoTracker(HiddenSidesPane parent) {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/org/dynamisfx/client/ModelInfo.fxml"));
            loader.setController(ModelInfoTracker.this);
            loader.setRoot(ModelInfoTracker.this);

            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(CustomWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.parentPane = parent;
        hideStatus.setFocusTraversable(false);
        hideStatus.setOnAction(e->{
            parentPane.setPinnedSide(null);
        });
        
        this.setOnMouseEntered(e->{
            if(parentPane.getPinnedSide() != Side.BOTTOM){
                parentPane.setPinnedSide(Side.BOTTOM);
            }
        });
    }
    
    public Label getSampleTitle() {
        return sampleTitle;
    }

    public Label getNodeCount() {
        return nodeCount;
    }

    public Label getTimeToBuild() {
        return timeToBuild;
    }

    public Label getBoundsWidth() {
        return width;
    }

    public Label getBoundsHeight() {
        return height;
    }

    public Label getBoundsDepth() {
        return depth;
    }

    public Label getPoints() {
        return points;
    }

    public Label getFaces() {
        return faces;
    }
}
