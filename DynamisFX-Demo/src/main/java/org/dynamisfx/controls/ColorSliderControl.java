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

import java.text.NumberFormat;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class ColorSliderControl extends ControlBase<Property<Number>> {

    public enum PrecisionString {
        DEFAULT,
        D_2,
        D_4,
        INT;

        private PrecisionString() {
        }
    }
    
    private final NumberFormat format = NumberFormat.getInstance();
    
    private final StringProperty colorBinding = new SimpleStringProperty();
    private final IntegerProperty colors = new SimpleIntegerProperty(this, "Colors", 1530){

        @Override
        protected void invalidated() {
            super.invalidated(); 
            Color color = Color.hsb(360*(1d-colors.get()/1530d), 1, 1);
            colorBinding.set(String.format("#%02X%02X%02X",
                (int)(color.getRed()*255),(int)(color.getGreen()*255),(int)(color.getBlue()*255)));
        }
        
    };
    
    public ColorSliderControl(final Property<Number> prop, final Number lowerBound, final Number upperBound) {
        super("/org/dynamisfx/controls/ColorSliderControl.fxml", prop);
        valSlider.getStyleClass().add("texture-slider");
        if(prop instanceof IntegerProperty){
            format.setMaximumFractionDigits(0);
            valSlider.setMin(lowerBound.intValue());
            valSlider.setMax(upperBound.intValue());
        }
        else if(prop instanceof DoubleProperty){
            format.setMaximumFractionDigits(2);
            valSlider.setMin(lowerBound.doubleValue());
            valSlider.setMax(upperBound.doubleValue());
        }
        valueLabel.textProperty().bind(colorBinding);
        if(controlledProperty==null){
            return;
        }
        valSlider.setValue(controlledProperty.getValue().doubleValue());
        
        colors.bind(valSlider.valueProperty());
        controlledProperty.bind(valSlider.valueProperty());
        propName.setText(!controlledProperty.getName().isEmpty() ? controlledProperty.getName() : "Empty Property Name:");
        
        valSlider.setShowTickLabels(false);
        valSlider.setShowTickMarks(false);
      
    }

    public Slider getSlider() {
        return valSlider;
    }

    @FXML
    private Label propName;
    @FXML
    private StackPane spacer;
    @FXML
    private Label valueLabel;
    @FXML
    private Slider valSlider;

}
