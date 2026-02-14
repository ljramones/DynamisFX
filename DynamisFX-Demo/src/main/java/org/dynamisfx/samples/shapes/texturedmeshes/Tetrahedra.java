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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import org.dynamisfx.controls.ControlCategory;
import org.dynamisfx.controls.NumberSliderControl;
import org.dynamisfx.controls.factory.ControlFactory;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.samples.shapes.TexturedMeshSample;
import org.dynamisfx.shapes.primitives.TetrahedraMesh;

/**
 *
 * @author jpereda
 */
public class Tetrahedra extends TexturedMeshSample{
    public static void main(String[] args){launch(args);}
    
    private final DoubleProperty height = new SimpleDoubleProperty(model, "Height", 12d) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((TetrahedraMesh)model).setHeight(height.get());
            }
        }
    };
    private final IntegerProperty level = new SimpleIntegerProperty(model, "Level", 2) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((TetrahedraMesh)model).setLevel(level.get());
            }
        }
    };
    @Override
    protected void createMesh() {
        model = new TetrahedraMesh(this.height.get(), this.level.get(), new Point3D(0f,0f,0f));
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
        
        NumberSliderControl levelSlider = ControlFactory.buildNumberSlider(this.level, 0, 8);
        levelSlider.getSlider().setMinorTickCount(0);
        levelSlider.getSlider().setMajorTickUnit(1);
        levelSlider.getSlider().setBlockIncrement(1);
        levelSlider.getSlider().setSnapToTicks(true);
        
        ControlCategory geomControls = ControlFactory.buildCategory("Geometry");
        geomControls.addControls(heightSlider,levelSlider);

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
