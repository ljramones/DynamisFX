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

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class TextFieldControl extends ControlBase<StringProperty> {

    public TextFieldControl(final String lbl, final StringProperty p) {
        super("/org/dynamisfx/controls/TextFieldControl.fxml", p);
        title.setText(lbl);
        selection.setText(p.getValue());
        controlledProperty.bind(selection.textProperty());
    }

    @FXML
    protected TextField selection;
    @FXML
    private Label title;

}
