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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import org.dynamisfx.controls.ControlCategory;
import org.dynamisfx.controls.NumberSliderControl;
import org.dynamisfx.controls.factory.ControlFactory;
import org.dynamisfx.samples.shapes.TexturedMeshSample;
import org.dynamisfx.shapes.primitives.CapsuleMesh;

/**
 *
 * @author Dub
 */
public class Capsule extends TexturedMeshSample{
    
    public static void main(String[] args){launch(args);}
        
    private final DoubleProperty radius = new SimpleDoubleProperty(model, "Radius", 50.0) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((CapsuleMesh)model).setRadius(radius.get());
            }
        }
    };
    private final DoubleProperty height = new SimpleDoubleProperty(model, "Height", 20.0) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((CapsuleMesh)model).setHeight(height.get());
            }
        }
    };
    
    private final IntegerProperty divisions = new SimpleIntegerProperty(model, "Divisions", 20) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((CapsuleMesh)model).setDivisions(divisions.get());
            }
        }
    };    
   
    @Override
    protected void createMesh() {
        model = new CapsuleMesh(divisions.getValue(), radius.getValue(), height.getValue());
    }

    @Override
    protected void addMeshAndListeners() {

    }

    @Override
    protected Node buildControlPanel() {
        //The radius of the capsule at its widest point (center)
        NumberSliderControl radSlider = ControlFactory.buildNumberSlider(radius, 5.0d, 150.0d);
        radSlider.getSlider().setMinorTickCount(10);
        radSlider.getSlider().setMajorTickUnit(25);
        radSlider.getSlider().setBlockIncrement(1);
        radSlider.getSlider().setSnapToTicks(true);       
        
        //The height of the capsule is also the length of the base cylinder form
        NumberSliderControl heightSlider = ControlFactory.buildNumberSlider(height, 10.0d, 400d);
        heightSlider.getSlider().setMinorTickCount(10);        
        heightSlider.getSlider().setMajorTickUnit(25);
        heightSlider.getSlider().setBlockIncrement(1);
        heightSlider.getSlider().setSnapToTicks(true);
        //The  divisions around the circumference of the Capsule
        NumberSliderControl divsSlider = ControlFactory.buildNumberSlider(divisions, 3D, 400D);
        divsSlider.getSlider().setMinorTickCount(5);
        divsSlider.getSlider().setMajorTickUnit(20);
        divsSlider.getSlider().setBlockIncrement(10d);
        divsSlider.getSlider().setSnapToTicks(true);
        
        ControlCategory geomControls = ControlFactory.buildCategory("Geometry");
        geomControls.addControls(radSlider, heightSlider, divsSlider);
        geomControls.setExpanded(true);

        this.controlPanel = ControlFactory.buildControlPanel(
                ControlFactory.buildMeshViewCategory(
                        this.drawMode,
                        this.culling
                ),
                geomControls,
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