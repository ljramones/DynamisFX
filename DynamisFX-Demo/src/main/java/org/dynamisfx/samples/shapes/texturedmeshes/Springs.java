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
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import org.dynamisfx.controls.ControlCategory;
import org.dynamisfx.controls.NumberSliderControl;
import org.dynamisfx.controls.factory.ControlFactory;
import org.dynamisfx.samples.shapes.TexturedMeshSample;
import org.dynamisfx.shapes.primitives.SpringMesh;

/**
 *
 * @author jpereda
 */
public class Springs extends TexturedMeshSample {

    public static void main(String[] args) {
        Springs.launch(args);
    }

    private final DoubleProperty meanRadius = new SimpleDoubleProperty(model, "Mean Radius", 5d) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SpringMesh)model).setMeanRadius(meanRadius.get());
            }
        }
    };
    
    private final DoubleProperty wireRadius = new SimpleDoubleProperty(model, "Wire Radius", 1d) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SpringMesh)model).setWireRadius(wireRadius.get());
            }
        }
    };
    
    private final DoubleProperty pitch = new SimpleDoubleProperty(model, "Pitch", 2d) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SpringMesh)model).setPitch(pitch.get());
            }
        }
    };
    
    private final DoubleProperty length = new SimpleDoubleProperty(model, "Length", 4d* 2d * Math.PI) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SpringMesh)model).setLength(length.get());
            }
        }
    };
    
    private final IntegerProperty wireDivs = new SimpleIntegerProperty(model, "Wire Divisions", 60) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SpringMesh)model).setWireDivisions(wireDivs.get());
            }
        }
    };
    private final IntegerProperty lenDivs = new SimpleIntegerProperty(model, "Length Divisions", 500) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SpringMesh)model).setLengthDivisions(lenDivs.get());
            }
        }
    };
    private final IntegerProperty wireCrop = new SimpleIntegerProperty(model, "Wire Crop", 0) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SpringMesh)model).setWireCrop((int)Math.min(wireCrop.get(),wireDivs.intValue()/2));
            }
        }
    };
    private final IntegerProperty lenCrop = new SimpleIntegerProperty(model, "Length Crop", 0) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SpringMesh)model).setLengthCrop((int)Math.min(lenCrop.intValue(),lenDivs.intValue()/2));
            }
        }

    };
    @Override
    protected void createMesh() {

        model = new SpringMesh(meanRadius.get(), wireRadius.get(), pitch.get(), length.get(),
                lenDivs.get(), wireDivs.get(), lenCrop.get(), wireCrop.get());
        model.setTextureModeNone(Color.ROYALBLUE);
        model.getTransforms().addAll(new Rotate(0, Rotate.X_AXIS), rotateY);
    }

    @Override
    protected void addMeshAndListeners() {
    }

    @Override
    protected Node buildControlPanel() {
        NumberSliderControl meanRadSlider = ControlFactory.buildNumberSlider(this.meanRadius, .01D, 200D);
        meanRadSlider.getSlider().setMinorTickCount(4);
        meanRadSlider.getSlider().setMajorTickUnit(25);
        meanRadSlider.getSlider().setBlockIncrement(1d);

        NumberSliderControl tRadSlider = ControlFactory.buildNumberSlider(this.wireRadius, 0.01D, 25D);
        tRadSlider.getSlider().setMinorTickCount(4);
        tRadSlider.getSlider().setMajorTickUnit(5);
        tRadSlider.getSlider().setBlockIncrement(0.5d);

        NumberSliderControl pitchSlider = ControlFactory.buildNumberSlider(this.pitch, 0.01D, 20D);
        pitchSlider.getSlider().setMinorTickCount(4);
        pitchSlider.getSlider().setMajorTickUnit(5);
        pitchSlider.getSlider().setBlockIncrement(0.5d);

        NumberSliderControl lengthSlider = ControlFactory.buildNumberSlider(this.length, 0.01D, 1000D);
        lengthSlider.getSlider().setMinorTickCount(4);
        lengthSlider.getSlider().setMajorTickUnit(50);
        lengthSlider.getSlider().setBlockIncrement(2d);

        NumberSliderControl wDivSlider = ControlFactory.buildNumberSlider(this.wireDivs, 2, 100);
        wDivSlider.getSlider().setMinorTickCount(4);
        wDivSlider.getSlider().setMajorTickUnit(10);
        wDivSlider.getSlider().setBlockIncrement(1);
        wDivSlider.getSlider().setSnapToTicks(true);

        NumberSliderControl mCropSlider = ControlFactory.buildNumberSlider(this.wireCrop, 0l, 98);
        mCropSlider.getSlider().setMinorTickCount(4);
        mCropSlider.getSlider().setMajorTickUnit(10);
        mCropSlider.getSlider().setBlockIncrement(1);
        mCropSlider.getSlider().setSnapToTicks(true);

        NumberSliderControl lDivSlider = ControlFactory.buildNumberSlider(this.lenDivs, 4l, 1000);
        lDivSlider.getSlider().setMinorTickCount(4);
        lDivSlider.getSlider().setMajorTickUnit(10);
        lDivSlider.getSlider().setBlockIncrement(1);
        lDivSlider.getSlider().setSnapToTicks(true);

        NumberSliderControl lCropSlider = ControlFactory.buildNumberSlider(this.lenCrop, 0l, 200);
        lCropSlider.getSlider().setMinorTickCount(4);
        lCropSlider.getSlider().setMajorTickUnit(10);
        lCropSlider.getSlider().setBlockIncrement(1);
        lCropSlider.getSlider().setSnapToTicks(true);

        ControlCategory geomControls = ControlFactory.buildCategory("Geometry");
        geomControls.addControls(meanRadSlider,
                tRadSlider, wDivSlider, pitchSlider, lengthSlider, mCropSlider,
                lDivSlider, lCropSlider);

        this.controlPanel = ControlFactory.buildControlPanel(
                ControlFactory.buildMeshViewCategory(
                        this.drawMode,
                        this.culling
                ),
                geomControls,
                ControlFactory.buildTextureMeshCategory(this.textureType, this.colors,
                        this.sectionType, this.textureImage,
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
