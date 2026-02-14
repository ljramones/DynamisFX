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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class ComboBoxControl<T> extends ControlBase<Property<T>> {

    public ComboBoxControl(final String lbl, final Property<T> p, final Collection<T> items, boolean subControl) {
        super("/org/dynamisfx/controls/ComboBoxControl.fxml", p);
        title.setText(lbl);
        selection.getItems().addAll(items);
        selection.setValue(p.getValue());
        controlledProperty.bind(selection.valueProperty());
        if (subControl) {
            subControlCache = FXCollections.observableHashMap();
            this.usesSubControls.set(subControl);
        }
    }

    @FXML
    protected ComboBox<T> selection;
    @FXML
    private Label title;
    @FXML
    private HBox hbox;
    @FXML
    private Pane spacer;
    @FXML
    protected VBox subControls;

    protected ObservableMap<T, List<ControlBase<Property<T>>>> subControlCache;

    public void addSubControl(final ControlBase ... controls) {
        if (useSubControls()) {
                subControlCache.putIfAbsent(selection.getValue(), Arrays.asList(controls));       
            //subControls.getChildren().add(subControlCache.get(control.controlledProperty));
        }
    }

    protected ObservableMap<T, List<ControlBase<Property<T>>>> getSubControlCache() {
        if (useSubControls()) {
            return subControlCache;
        } else {
            return null;
        }
    }

    private final BooleanProperty usesSubControls = new SimpleBooleanProperty(this, "usesSubControls", false) {

        @Override
        protected void invalidated() {
            super.invalidated();

        }

    };

    protected boolean useSubControls() {
        return usesSubControls.get();
    }

    public BooleanProperty usingSubControlsProperty() {
        return usesSubControls;
    }

}
