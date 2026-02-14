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
import org.dynamisfx.shapes.primitives.SegmentedSphereMesh;

/**
 *
 * @author Sean
 */
public class SegmentedSphere extends TexturedMeshSample {

    public static void main(String[] args){launch(args);}

    private final DoubleProperty radius = new SimpleDoubleProperty(model, "Radius", 50.0) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedSphereMesh)model).setRadius(radius.get());
            }
        }
    };
    private final IntegerProperty cropX = new SimpleIntegerProperty(model, "Radius Crop X", 0) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedSphereMesh)model).setRadiusCropX(cropX.get());
            }
        }
    };
    private final IntegerProperty cropY = new SimpleIntegerProperty(model, "Radius Crop Y", 0) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedSphereMesh)model).setRadiusCropY(cropY.get());
            }
        }
    };
    private final IntegerProperty divisions = new SimpleIntegerProperty(model, "Radius Divisions", 20) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedSphereMesh)model).setRadiusDivisions(divisions.get());
            }
        }
    };

    
    @Override
    protected void createMesh() {
        model = new SegmentedSphereMesh(divisions.getValue(), cropX.getValue(), cropY.getValue(), radius.getValue(), new Point3D(0f, 0f, 0f));
    }

    @Override
    protected void addMeshAndListeners() {
        
    }

    @Override
    protected Node buildControlPanel() {

        NumberSliderControl radSlider = ControlFactory.buildNumberSlider(radius, .01D, 200D);
        radSlider.getSlider().setMinorTickCount(10);
        radSlider.getSlider().setMajorTickUnit(0.5);
        radSlider.getSlider().setBlockIncrement(0.01d);

        NumberSliderControl cropXSlider = ControlFactory.buildNumberSlider(cropX, 0, 5);
        cropXSlider.getSlider().setBlockIncrement(1);

        NumberSliderControl cropYSlider = ControlFactory.buildNumberSlider(cropY, 0, 5);
        cropYSlider.getSlider().setBlockIncrement(1);

        NumberSliderControl divsSlider = ControlFactory.buildNumberSlider(divisions, 5, 200);
        divsSlider.getSlider().setMinorTickCount(4);
        divsSlider.getSlider().setMajorTickUnit(5);
        divsSlider.getSlider().setBlockIncrement(5);
        divsSlider.getSlider().valueProperty().addListener((obs, ov, nv) -> {
            cropXSlider.getSlider().setValue(Math.min(cropXSlider.getSlider().getValue(), nv.intValue() / 2 - 1));
            cropXSlider.getSlider().setMax(nv.intValue() / 2 - 1);
            cropYSlider.getSlider().setValue(Math.min(cropYSlider.getSlider().getValue(), nv.intValue() / 2 - 1));
            cropYSlider.getSlider().setMax(nv.intValue() / 2 - 1);
        });
        
        ControlCategory geomControls = ControlFactory.buildCategory("Geometry");
        geomControls.addControls(radSlider, cropXSlider, cropYSlider, divsSlider);

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