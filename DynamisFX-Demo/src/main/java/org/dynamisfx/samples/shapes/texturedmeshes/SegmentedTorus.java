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
import javafx.scene.transform.Rotate;
import org.dynamisfx.controls.ControlCategory;
import org.dynamisfx.controls.factory.ControlFactory;
import org.dynamisfx.samples.shapes.TexturedMeshSample;
import org.dynamisfx.shapes.primitives.SegmentedTorusMesh;

/**
 *
 * @author jpereda
 */
public class SegmentedTorus extends TexturedMeshSample {
    public static void main(String[] args){SegmentedTorus.launch(args);}
    
    private final DoubleProperty majRad = new SimpleDoubleProperty(model, "Major Radius", 2) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedTorusMesh)model).setMajorRadius(majRad.get());
            }
        }
    };
    private final DoubleProperty minRad = new SimpleDoubleProperty(model, "Minor Radius", 1) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedTorusMesh)model).setMinorRadius(minRad.get());
            }
        }
    };
    private final IntegerProperty majorDivs = new SimpleIntegerProperty(model, "Major Radius Divisions", 100) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedTorusMesh)model).setMajorRadiusDivisions(majorDivs.get());
            }
        }
    };
    private final IntegerProperty minorDivs = new SimpleIntegerProperty(model, "Minor Radius Divisions", 100) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedTorusMesh)model).setMinorRadiusDivisions(minorDivs.get());
            }
        }
    };
    private final DoubleProperty _x = new SimpleDoubleProperty(model, "X Offset") {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedTorusMesh)model).setxOffset(_x.doubleValue());
            }
        }
    };
    private final DoubleProperty _y = new SimpleDoubleProperty(model, "Y Offset") {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedTorusMesh)model).setyOffset(_y.doubleValue());
            }
        }
    };
    private final DoubleProperty _z = new SimpleDoubleProperty(model, "Z Offset",1d) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedTorusMesh)model).setzOffset(_z.doubleValue());
            }
        }
    };

    private final IntegerProperty majRadCrop = new SimpleIntegerProperty(model, "Major Radius Crop") {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedTorusMesh)model).setMajorRadiusCrop(majRadCrop.getValue());
            }
        }
    };
    
    
    @Override
    public void createMesh() {
//        model = new SegmentedTorusMesh(50, 42, 0, 100d, 25d);        
        model = new SegmentedTorusMesh(majorDivs.get(), minorDivs.get(), majRadCrop.get(), majRad.get(), minRad.get());        
        model.getTransforms().addAll(new Rotate(0, Rotate.X_AXIS), rotateY);
    }
    
    @Override
    protected void addMeshAndListeners() {
    }
    
    @Override
    protected Node buildControlPanel() {
        ControlCategory geomControls = ControlFactory.buildCategory("Geometry");
        geomControls.addControls(
                ControlFactory.buildNumberSlider(majRad, 1, 100),
                ControlFactory.buildNumberSlider(minRad, 1, 100),
                ControlFactory.buildNumberSlider(majorDivs, 8, 360),
                ControlFactory.buildNumberSlider(minorDivs, 8, 360),
                ControlFactory.buildNumberSlider(majRadCrop, 0, 50),
                ControlFactory.buildNumberSlider(_x, -1, 1),
                ControlFactory.buildNumberSlider(_y, -1, 1),
                ControlFactory.buildNumberSlider(_z, 0.01, 100)
                //ControlFactory.buildNumberSlider(_angle, 0.01, 359.89)                
        );

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
