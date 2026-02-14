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

package org.dynamisfx.shapes;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import org.dynamisfx.shapes.containers.ShapeContainer;
import org.dynamisfx.shapes.primitives.ConeMesh;

/**
 *
 * @author Birdasaur
 * adapted Dub's Capsule class served as an example framework to adapt 
 * my existing Cone code.
 */
public class Cone extends ShapeContainer<ConeMesh>{
     private ConeMesh mesh;
    
    public Cone() {
        super(new ConeMesh());
        this.mesh = getShape();
    }
    public Cone(int divisions, double radius, double height){
        this();
        mesh.setDivisions(divisions);
        mesh.setRadius(radius);
        mesh.setHeight(height);        
    }
    public Cone(Color c){
        this();
        this.setDiffuseColor(c);
    }
    public Cone(int divisions, double radius, double height, Color c){
        //this(divisions, radius, height);
        super(new ConeMesh(divisions, radius, height));
        this.mesh = getShape();
        this.setDiffuseColor(c);
    }
    public final void setRadius(double value) {
        mesh.setRadius(value);
    }
    public final void setHeight(double value) {
        mesh.setHeight(value);
    }
    public final void setDivisions(int value) {
        mesh.setDivisions(value);
    }
    public final void setMaterial(Material value) {
        mesh.setMaterial(value);
    }
    public final void setDrawMode(DrawMode value) {
        mesh.setDrawMode(value);
    }
    public final void setCullFace(CullFace value) {
        mesh.setCullFace(value);
    }
    public final double getRadius() {
        return mesh.getRadius();
    }
    public DoubleProperty radiusProperty() {
        return mesh.radiusProperty();
    }
    public final double getHeight() {
        return mesh.getHeight();
    }
    public DoubleProperty heightProperty() {
        return mesh.heightProperty();
    }
    public final int getDivisions() {
        return mesh.getDivisions();
    }
    public IntegerProperty divisionsProperty() {
        return mesh.divisionsProperty();
    }       
}