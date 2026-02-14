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

import java.util.function.Function;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import org.dynamisfx.controls.ControlCategory;
import org.dynamisfx.controls.factory.ControlFactory;
import org.dynamisfx.samples.shapes.TexturedMeshSample;
import org.dynamisfx.shapes.primitives.SurfacePlotMesh;

/**
 *
 * @author jpereda
 */
public class SurfacePlot extends TexturedMeshSample {
    public static void main(String[] args){SurfacePlot.launch(args);}
    
    //private static final Image image = new Image(SurfacePlot.class.getResourceAsStream(".../res/top.png"));
    private final ObjectProperty<Function<Point2D, Number>> function2D = 
            new SimpleObjectProperty<Function<Point2D, Number>>(model,"Function F(P(x,y))",p->Math.sin(p.magnitude())){
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SurfacePlotMesh)model).setFunction2D(function2D.get());
            }
        }
    };
    private final DoubleProperty rangeX = new SimpleDoubleProperty(model, "Range X", 20) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SurfacePlotMesh)model).setRangeX(rangeX.get());
            }
        }
    };

    private final DoubleProperty rangeY = new SimpleDoubleProperty(model, "Range Y", 20) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SurfacePlotMesh)model).setRangeY(rangeY.get());
            }
        }
    };

    private final IntegerProperty divisionsX = new SimpleIntegerProperty(model, "Divisions X", 100) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SurfacePlotMesh)model).setDivisionsX(divisionsX.get());
            }
        }
    };
    
    private final IntegerProperty divisionsY = new SimpleIntegerProperty(model, "Divisions Y", 100) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SurfacePlotMesh)model).setDivisionsY(divisionsY.get());
            }
        }
    };
    
    private final DoubleProperty scale = new SimpleDoubleProperty(model, "Scale", 2) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((SurfacePlotMesh)model).setFunctionScale(scale.get());
            }
        }
    };

    
    @Override
    public void createMesh() {
        model = new SurfacePlotMesh(function2D.get(),rangeX.get(),rangeY.get(),divisionsX.get(),divisionsY.get(),scale.get());        
        model.getTransforms().addAll(new Rotate(0, Rotate.X_AXIS), rotateY);
        model.sceneProperty().addListener(e->{
            if(model.getScene()!= null){
                //material.setDiffuseMap(image);
            }
        });
        
    }
    
    @Override
    protected void addMeshAndListeners() {
    }
    
    @Override
    protected Node buildControlPanel() {
        ControlCategory geomControls = ControlFactory.buildCategory("Geometry");
        geomControls.addControls(
                ControlFactory.buildScriptFunction2DControl(function2D),
                ControlFactory.buildNumberSlider(rangeX, 0, 100),
                ControlFactory.buildNumberSlider(rangeY, 0, 100),
                ControlFactory.buildNumberSlider(divisionsX, 1, 1000),
                ControlFactory.buildNumberSlider(divisionsY, 1, 1000),
                ControlFactory.buildNumberSlider(scale, 0.01, 100)
        );

        this.controlPanel = ControlFactory.buildControlPanel(
                ControlFactory.buildMeshViewCategory(
                        this.drawMode,
                        this.culling
                ),
                geomControls,
                ControlFactory.buildTextureMeshCategory(this.textureType,
                        this.colors,
                        null,
                        this.textureImage,
                        this.useBumpMap, 
                        this.bumpScale,
                        this.bumpFineScale, 
                        this.invert,
                        this.patterns,
                        this.pattScale,
                        this.specColor, 
                        this.specularPower, 
                        this.dens,
                        this.func
                )
        );
        
        return this.controlPanel;
    }

    
    
}
