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
import org.dynamisfx.samples.shapes.TexturedMeshSample;
import org.dynamisfx.shapes.primitives.SegmentedDomeMesh;

/**
 *
 * @author Sean
 */
public class SegmentedDome extends TexturedMeshSample {

    public static void main(String[] args){launch(args);}

    private final DoubleProperty radius = new SimpleDoubleProperty(model, "Radius", 50.0) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedDomeMesh)model).setRadius(radius.get());
            }
        }
    };
    private final DoubleProperty phiMin = new SimpleDoubleProperty(model, "PhiMin", Math.toRadians(0)) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedDomeMesh)model).setPhimin(phiMin.get());
            }
        }
    };
    private final DoubleProperty phiMax = new SimpleDoubleProperty(model, "PhiMax", Math.toRadians(360)) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedDomeMesh)model).setPhimax(phiMax.get());
            }
        }
    };
    private final DoubleProperty thetaMin = new SimpleDoubleProperty(model, "ThetaMin", Math.toRadians(0)) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedDomeMesh)model).setThetamin(thetaMin.get());
            }
        }
    };
    private final DoubleProperty thetaMax = new SimpleDoubleProperty(model, "ThetaMax", Math.toRadians(90)) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedDomeMesh)model).setThetamax(thetaMax.get());
            }
        }
    };
    private final IntegerProperty divisions = new SimpleIntegerProperty(model, "Divisions", 20) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SegmentedDomeMesh)model).setDivisions(divisions.get());
            }
        }
    };

    
    @Override
    protected void createMesh() {
        model = new SegmentedDomeMesh(radius.getValue(), phiMin.getValue(), phiMax.getValue(),
            thetaMin.getValue(), thetaMax.getValue(), divisions.getValue());
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

        NumberSliderControl phiMinSlider = ControlFactory.buildNumberSlider(phiMin, Math.toRadians(0), Math.toRadians(360));
        phiMinSlider.getSlider().setMinorTickCount(10);
        phiMinSlider.getSlider().setMajorTickUnit(0.5);
        phiMinSlider.getSlider().setBlockIncrement(0.01d);

        NumberSliderControl phiMaxSlider = ControlFactory.buildNumberSlider(phiMax, Math.toRadians(0), Math.toRadians(360));
        phiMaxSlider.getSlider().setMinorTickCount(10);
        phiMaxSlider.getSlider().setMajorTickUnit(0.5);
        phiMaxSlider.getSlider().setBlockIncrement(0.01d);
        
        NumberSliderControl thetaMinSlider = ControlFactory.buildNumberSlider(thetaMin, Math.toRadians(0), Math.toRadians(90));
        thetaMinSlider.getSlider().setMinorTickCount(10);
        thetaMinSlider.getSlider().setMajorTickUnit(0.5);
        thetaMinSlider.getSlider().setBlockIncrement(0.01d);

        NumberSliderControl thetaMaxSlider = ControlFactory.buildNumberSlider(thetaMax, Math.toRadians(0), Math.toRadians(90));
        thetaMaxSlider.getSlider().setMinorTickCount(10);
        thetaMaxSlider.getSlider().setMajorTickUnit(0.5);
        thetaMaxSlider.getSlider().setBlockIncrement(0.01d);
        
        NumberSliderControl divsSlider = ControlFactory.buildNumberSlider(divisions, 5, 200D);
        divsSlider.getSlider().setMinorTickCount(4);
        divsSlider.getSlider().setMajorTickUnit(5);
        divsSlider.getSlider().setBlockIncrement(5);
        
        ControlCategory geomControls = ControlFactory.buildCategory("Geometry");
        geomControls.addControls(radSlider, phiMinSlider, phiMaxSlider, 
            thetaMinSlider, thetaMaxSlider, divsSlider);

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