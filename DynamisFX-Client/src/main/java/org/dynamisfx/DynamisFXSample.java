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

import javafx.scene.Node;
import javafx.stage.Stage;

/**
 */
public interface DynamisFXSample {

    /**
     * @return A short, most likely single-word, name to show to the user - e.g. "CheckBox"
     */
    public String getSampleName();

    /**
     * @return A short, multiple sentence description of the sample.
     */
    public String getSampleDescription();
    
    /**
     * @return the name of the project that this sample belongs to (e.g. 'JFXtras' or 'ControlsFX').
     */
    public String getProjectName();
    
    /**
     * @return the version of the project that this sample belongs to (e.g. '1.0.0')
     */
    public String getProjectVersion();
    
    /**
     * @param stage
     * @return the main sample panel.
     */
    public Node getPanel(final Stage stage);

    /**
     * @return the panel to display to the user that allows for manipulating the sample.
     */
    public Node getControlPanel();
    
    /*
     * Returns divider position to use for split between main panel and control panel 
     * @return 
     */
    //public double getControlPanelDividerPosition();

    /**
     * @return A full URL to the javadoc for the API being demonstrated in this sample.
     */
    public String getJavaDocURL();
    
    /**
     * @return URL for control's stylesheet
     */
    public String getControlStylesheetURL();
    
    /**
     * @return A full URL to a sample source code, which is assumed to be in java.
     */
    public String getSampleSourceURL();
    
    /**
     * @return If true this sample is shown to users, if false it is not.
     */
    public boolean isVisible();

    /**
     * @return Optional transform gizmo controls for this sample, if supported.
     */
    default SampleGizmoSupport getGizmoSupport() {
        return null;
    }

}
