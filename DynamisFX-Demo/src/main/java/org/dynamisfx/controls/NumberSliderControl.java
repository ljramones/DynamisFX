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

import java.text.NumberFormat;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Jason Pollastrini aka jdub1581
 */
public class NumberSliderControl extends ControlBase<Property<Number>> {
    
    private final NumberFormat format = NumberFormat.getInstance();
    
    public NumberSliderControl(final Property<Number> prop, final Number lowerBound, final Number upperBound) {
        super("/org/dynamisfx/controls/NumberSliderControl.fxml", prop);
        
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
        valueLabel.textProperty().bindBidirectional(valSlider.valueProperty(),format);
        if(controlledProperty==null){
            return;
        }
        valSlider.setValue(controlledProperty.getValue().doubleValue());
        
        // PENDING
//        valSlider.valueProperty().addListener((ov,i,i1)->{
//            if(!valSlider.isValueChanging()){
//                controlledProperty.setValue(i1);
//            }
//        });
//        valSlider.valueChangingProperty().addListener((ov,b,b1)->{
//            if(!b1){
//               controlledProperty.setValue(valSlider.getValue());
//            }
//        });
        controlledProperty.bind(valSlider.valueProperty());
        propName.setText(!controlledProperty.getName().isEmpty() ? controlledProperty.getName() : "Empty Property Name:");
      
        
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
