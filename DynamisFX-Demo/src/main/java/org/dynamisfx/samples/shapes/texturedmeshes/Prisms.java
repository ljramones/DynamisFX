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

import java.util.List;
import java.util.stream.Collectors;
import static javafx.application.Application.launch;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;
import org.dynamisfx.controls.CheckBoxControl;
import org.dynamisfx.controls.ControlCategory;
import org.dynamisfx.controls.NumberSliderControl;
import org.dynamisfx.controls.factory.ControlFactory;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.samples.shapes.TexturedMeshSample;
import org.dynamisfx.shapes.primitives.PrismMesh;

/**
 *
 * @author jpereda
 */
public class Prisms extends TexturedMeshSample{
    
    public static void main(String[] args){
        launch(args);
    }
    
    private PrismMesh fake;
    
    private final BooleanProperty showKnots = new SimpleBooleanProperty(this, "Show Knots");
    private final BooleanProperty enablePicking = new SimpleBooleanProperty(this, "Allow Knots Dragging");
    private final BooleanProperty pickingOnDragging = new SimpleBooleanProperty(this, "Update Prism on Dragging");
    
    private final DoubleProperty radius = new SimpleDoubleProperty(model, "Radius", 1d) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((PrismMesh)model).setRadius(radius.get());
            }
        }
    };
    private final DoubleProperty height = new SimpleDoubleProperty(model, "Height", 3d) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((PrismMesh)model).setHeight(height.get());
            }
        }
    };
    private final IntegerProperty level = new SimpleIntegerProperty(model, "Level", 2) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (model != null) {
                ((PrismMesh)model).setLevel(level.get());
            }
        }
    };
    @Override
    protected void createMesh() {
        model = new PrismMesh(this.radius.get(), this.height.get(), this.level.get());
        model.setTextureModeNone(Color.ROYALBLUE);
    }

    private List<Point3D> updateKnotsList(){
        return group.getChildren().stream().filter(Sphere.class::isInstance)
                .filter(n->n.getId().equals("knot"))
                .map(s->s.localToParent(new javafx.geometry.Point3D(0, 0, 0)))
                .map(p->new Point3D((float)p.getX(),(float)p.getY(),(float)p.getZ()))
                .collect(Collectors.toList());
    }
    
    @Override
    protected void addMeshAndListeners() {
        
        enablePicking.addListener((obs,b,b1)->{
            group.getChildren().stream()
                    .filter(Sphere.class::isInstance)
                    .forEach(s->s.setId(b1?"knot":""));
        });
        
        showKnots.addListener((obs, b, b1) ->{
            if (b1) {
                Point3D k0 = ((PrismMesh)model).getAxisOrigin();
                Point3D k3 = ((PrismMesh)model).getAxisEnd();
                final Sphere s = new Sphere(0.2d);
                s.setId("");
                s.getTransforms().add(new Translate(k0.x, k0.y, k0.z));
                s.setMaterial(new PhongMaterial(Color.ROSYBROWN));
                group.getChildren().add(s);
                final Sphere s2 = new Sphere(0.2d);
                s2.setId("");
                s2.getTransforms().add(new Translate(k3.x, k3.y, k3.z));
                s2.setMaterial(new PhongMaterial(Color.GREENYELLOW));
                group.getChildren().add(s2);
                s.getTransforms().addListener((Observable observable) -> {
                    javafx.geometry.Point3D p=s.localToParent(new javafx.geometry.Point3D(0, 0, 0));
                    if(pickingOnDragging.get()){
                        ((PrismMesh)model).setAxisOrigin(new Point3D((float)p.getX(),(float)p.getY(),(float)p.getZ()));
                    } else if(fake!=null){
                        fake.setAxisOrigin(new Point3D((float)p.getX(),(float)p.getY(),(float)p.getZ()));
                    }
                });
                s2.getTransforms().addListener((Observable observable) -> {
                    javafx.geometry.Point3D p=s2.localToParent(new javafx.geometry.Point3D(0, 0, 0));
                    if(pickingOnDragging.get()){
                        ((PrismMesh)model).setAxisEnd(new Point3D((float)p.getX(),(float)p.getY(),(float)p.getZ()));
                    } else if(fake!=null){
                        fake.setAxisEnd(new Point3D((float)p.getX(),(float)p.getY(),(float)p.getZ()));
                    }
                });
            } else {
                group.getChildren().removeIf(Sphere.class::isInstance);
                chkEnablePicking.setSelected(false);
                chkPickingOnDragging.setSelected(false);
            }
        });
        
        pickingProperty().addListener((obs,b,b1)->{
            if(b1 && !pickingOnDragging.get()){ // start picking
                fake=new PrismMesh(((PrismMesh)model).getRadius(),1,0,((PrismMesh)model).getAxisOrigin(),((PrismMesh)model).getAxisEnd());
                fake.setSectionType(((PrismMesh)model).getSectionType());
                fake.setDrawMode(DrawMode.LINE);
                fake.setId("fake");
                group.getChildren().add(fake);
            }
            if(b && !b1){ // after picking
                if(!pickingOnDragging.get()){
                    group.getChildren().removeIf(n->n.getId()!=null && n.getId().equals("fake"));
                    fake=null;
                }
                List<Point3D> list = updateKnotsList();
                if(list!=null && list.size()==2){
                    if(!list.get(0).equals(((PrismMesh)model).getAxisOrigin())){
                        ((PrismMesh)model).setAxisOrigin(list.get(0));
                    } else if(!list.get(1).equals(((PrismMesh)model).getAxisEnd())){
                        ((PrismMesh)model).setAxisEnd(list.get(1));
                    }
                }
                
            }
        });
    }

    private final CheckBoxControl chkKnots = ControlFactory.buildCheckBoxControl(showKnots);
    private final CheckBoxControl chkEnablePicking = ControlFactory.buildCheckBoxControl(enablePicking);
    private final CheckBoxControl chkPickingOnDragging = ControlFactory.buildCheckBoxControl(pickingOnDragging);
        
    @Override
    protected Node buildControlPanel() {
        chkEnablePicking.disableProperty().bind(showKnots.not());
        chkPickingOnDragging.disableProperty().bind(showKnots.not().or(enablePicking.not()));
      
        NumberSliderControl radiusSlider = ControlFactory.buildNumberSlider(this.radius, .01D, 200D);
        radiusSlider.getSlider().setMinorTickCount(10);
        radiusSlider.getSlider().setMajorTickUnit(0.5);
        radiusSlider.getSlider().setBlockIncrement(0.01d);

//        NumberSliderControl heightSlider = ControlFactory.buildNumberSlider(this.height, .01D, 200D);
//        heightSlider.getSlider().setMinorTickCount(10);
//        heightSlider.getSlider().setMajorTickUnit(0.5);
//        heightSlider.getSlider().setBlockIncrement(0.01d);
        
        NumberSliderControl levelSlider = ControlFactory.buildNumberSlider(this.level, 0, 8);
        levelSlider.getSlider().setMinorTickCount(0);
        levelSlider.getSlider().setMajorTickUnit(1);
        levelSlider.getSlider().setBlockIncrement(1);
        levelSlider.getSlider().setSnapToTicks(true);
        
        ControlCategory geomControls = ControlFactory.buildCategory("Geometry");
        geomControls.addControls(chkKnots,chkEnablePicking,chkPickingOnDragging,
                radiusSlider,levelSlider);

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
