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

package org.dynamisfx.samples.shapes.texturedmeshes;

import static javafx.application.Application.launch;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import org.dynamisfx.controls.ControlCategory;
import org.dynamisfx.controls.NumberSliderControl;
import org.dynamisfx.controls.factory.ControlFactory;
import org.dynamisfx.samples.shapes.GroupOfTexturedMeshSample;
import org.dynamisfx.shapes.primitives.Text3DMesh;

/**
 *
 * @author jpereda
 */
public class Text3D extends GroupOfTexturedMeshSample{
    
    public static void main(String[] args){launch(args);}
    
    private final DoubleProperty height = new SimpleDoubleProperty(model, "Height", 12d) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((Text3DMesh)model).setHeight(get());
            }
        }
    };
    private final DoubleProperty gap = new SimpleDoubleProperty(model, "Gap", 0d) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((Text3DMesh)model).setGap(get());
            }
        }
    };
    private final IntegerProperty level = new SimpleIntegerProperty(model, "Level", 2) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((Text3DMesh)model).setLevel(get());
            }
        }
    };
    private final IntegerProperty fontSize = new SimpleIntegerProperty(model, "Font Size", 100) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((Text3DMesh)model).setFontSize(get());
            }
        }
    };
    private final BooleanProperty joinSegments = new SimpleBooleanProperty(model, "Join Segments", false) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((Text3DMesh)model).setJoinSegments(get());
            }
        }
    };
    private final StringProperty text3D = new SimpleStringProperty(model, "Text3D", "DynamisFX") {
        @Override
        protected void invalidated() {
            super.invalidated(); 
            System.out.println("text " + get());
            if (model != null) {
                ((Text3DMesh)model).setText3D(get());
            }
        }
    }; 
    private final StringProperty font = new SimpleStringProperty(model, "Font Family", "Arial") {
        @Override
        protected void invalidated() {
            super.invalidated(); 
            System.out.println("font " + get());
            if (model != null) {
                ((Text3DMesh)model).setFont(get());
            }
        }
    }; 
    
    @Override
    protected void createMesh() {
        model = new Text3DMesh(this.text3D.get(), this.height.get(), this.level.get());
//        model.setTextureModeNone(Color.ROYALBLUE);
    }


    @Override
    protected void addMeshAndListeners() {
    }

    @Override
    protected Node buildControlPanel() {
        NumberSliderControl heightSlider = ControlFactory.buildNumberSlider(this.height, .01D, 200D);
        heightSlider.getSlider().setMinorTickCount(10);
        heightSlider.getSlider().setMajorTickUnit(0.5);
        heightSlider.getSlider().setBlockIncrement(0.01d);
        
        NumberSliderControl gapSlider = ControlFactory.buildNumberSlider(this.gap, 0D, 100D);
        gapSlider.getSlider().setMinorTickCount(4);
        gapSlider.getSlider().setMajorTickUnit(5);
        gapSlider.getSlider().setBlockIncrement(1d);
        
        NumberSliderControl fontSizeSlider = ControlFactory.buildNumberSlider(this.fontSize, 1D, 400D);
        fontSizeSlider.getSlider().setMinorTickCount(1);
        fontSizeSlider.getSlider().setMajorTickUnit(10d);
        fontSizeSlider.getSlider().setBlockIncrement(1d);
        
        NumberSliderControl levelSlider = ControlFactory.buildNumberSlider(this.level, 0, 8);
        levelSlider.getSlider().setMinorTickCount(0);
        levelSlider.getSlider().setMajorTickUnit(1);
        levelSlider.getSlider().setBlockIncrement(1);
        levelSlider.getSlider().setSnapToTicks(true);
        
        ControlCategory geomControls = ControlFactory.buildCategory("Geometry");
        geomControls.addControls(heightSlider, gapSlider, fontSizeSlider,levelSlider);

        ControlCategory text3DControls = ControlFactory.buildCategory("Text");
        text3DControls.addControls(ControlFactory.buildFontControl(this.font), 
                ControlFactory.buildTextFieldControl("Text", text3D), 
                ControlFactory.buildCheckBoxControl(joinSegments));

        this.controlPanel = ControlFactory.buildControlPanel(
                ControlFactory.buildMeshViewCategory(
                        this.drawMode,
                        this.culling
                ),
                geomControls, text3DControls,
                ControlFactory.buildTextureMeshCategory(this.textureType, this.colors, 
                        null, this.textureImage,
                        this.useBumpMap, this.bumpScale,
                        this.bumpFineScale, this.invert,
                        this.patterns, this.pattScale, 
                        this.specColor, this.specularPower, 
                        this.dens, this.func
                )
        );
        
        return this.controlPanel;
    }

}
