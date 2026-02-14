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

import javafx.scene.Node;
import org.dynamisfx.controls.ControlCategory;
import org.dynamisfx.controls.NumberSliderControl;
import org.dynamisfx.controls.factory.ControlFactory;
import org.dynamisfx.samples.shapes.TexturedMeshSample;
import org.dynamisfx.shapes.primitives.ConeMesh;
import org.reactfx.value.Var;

/**
 *
 * @author Birdasaur
 * adapted jDub's Capsule Test merged with my original Cone Test
 */
public class Cones extends TexturedMeshSample {
    
    public static void main(String[] args){launch(args);}
    
    protected Var<Integer> divisions = Var.newSimpleVar(64);
    
    protected Var<Double> radius = Var.newSimpleVar(50.0);
    
    protected Var<Double> height = Var.newSimpleVar(75.0);
    
    
    
    @Override
    protected void createMesh() {
        model = new ConeMesh(divisions.getValue(), radius.getValue(), height.getValue());
    }

    @Override
    protected void addMeshAndListeners() {
        
    }

    @Override
    protected Node buildControlPanel() {
        NumberSliderControl divsSlider = ControlFactory.buildNumberSlider(null, .01D, 200D);
        divsSlider.getSlider().setMinorTickCount(10);
        divsSlider.getSlider().setMajorTickUnit(0.5);
        divsSlider.getSlider().setBlockIncrement(0.01d);

        NumberSliderControl heightSlider = ControlFactory.buildNumberSlider(null, .01D, 200D);
        heightSlider.getSlider().setMinorTickCount(10);
        heightSlider.getSlider().setMajorTickUnit(0.5);
        heightSlider.getSlider().setBlockIncrement(0.01d);
        
        NumberSliderControl radSlider = ControlFactory.buildNumberSlider(null, .01D, 200D);
        radSlider.getSlider().setMinorTickCount(10);
        radSlider.getSlider().setMajorTickUnit(0.5);
        radSlider.getSlider().setBlockIncrement(0.01d);
        
        
        
        ControlCategory geomControls = ControlFactory.buildCategory("Geometry");
        //geomControls.addControls(widthSlider,heightSlider,depthSlider,levelSlider);

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
