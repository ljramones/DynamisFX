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

package org.dynamisfx;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * A base class for samples - it is recommended that they extend this class
 * rather than Application, as then the samples can be run either standalone
 * or within FXSampler. 
 */
public abstract class DynamisFXSampleBase extends Application implements DynamisFXSample {
    
    /** {@inheritDoc}
     * @throws java.lang.Exception */
    @Override public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(getSampleName());
        
        Scene scene = new Scene((Parent)buildSample(this, primaryStage), 800, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /** {@inheritDoc}
     * @return  */
    @Override public boolean isVisible() {
        return true;
    }
    
    /** {@inheritDoc}
     * @return  */
    @Override public Node getControlPanel() {
        return null;
    }
    
    /**
     * @return  */
    public double getControlPanelDividerPosition() {
    	return 0.6;
    }
    
    /** {@inheritDoc}
     * @return  */
    @Override public String getSampleDescription() {
        return "";
    }
    
    /** {@inheritDoc}
     * @return  */
    @Override public String getProjectName() {
        return "DynamisFX-Demo";
    }
    
    /**
     * Utility method to create the default look for samples.
     * 
     * This is also where the service should be ran from or the changeSample method in DynamisFXSampler
     * 
     * @param sample
     * @param stage
     * @return 
     */
    public static Node buildSample(DynamisFXSample sample, Stage stage) {                
        return sample.getPanel(stage);
    }
}
