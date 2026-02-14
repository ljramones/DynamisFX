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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;

/**
 * FXML Controller class
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class ControlBasePane extends Accordion{
    
    public ControlBasePane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dynamisfx/controls/ControlBasePane.fxml"));
            loader.setRoot(ControlBasePane.this);
            loader.setController(ControlBasePane.this);
            loader.load();
        } catch (IOException ex) {
            
            Logger.getLogger(CheckBoxControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
