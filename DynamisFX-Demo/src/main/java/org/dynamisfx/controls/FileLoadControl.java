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

import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author Jose Pereda
 */
public class FileLoadControl extends ControlBase<Property<URL>> {

    @FXML private TextField filePathTf;

    public FileLoadControl(Property<URL> prop) {
        super("/org/dynamisfx/controls/FileLoadControl.fxml", prop);
        filePathTf.setEditable(false);
    }

    @FXML
    private void chooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(filePathTf.getScene().getWindow());
        if (file != null) {
            filePathTf.setText(file.getAbsolutePath());
            try {
                controlledProperty.setValue(file.toURI().toURL());
            } catch (MalformedURLException e) {
                Logger.getLogger(FileLoadControl.class.getName()).log(Level.SEVERE,null, e);
            }
        }
    }

}
