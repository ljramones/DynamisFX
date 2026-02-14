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
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import org.dynamisfx.shapes.containers.ShapeContainer;
import org.dynamisfx.shapes.primitives.SpheroidMesh;

/**
 *
 * @author jdub1581
 */
public class Spheroid extends ShapeContainer<SpheroidMesh> {
    private SpheroidMesh mesh;
    public Spheroid() {
        super(new SpheroidMesh());
        mesh = getShape();        
    }

    public Spheroid(double radius) {
        this();
        mesh.setMinorRadius( radius);
        mesh.setMajorRadius( radius);
    }

    public Spheroid(double minorRadius, double majorRadius) {
        this();
        mesh.setMinorRadius(minorRadius);
        mesh.setMajorRadius(majorRadius);
    }
    
    public Spheroid(int divisions, double minorRadius, double majorRadius) {
        this();
        mesh.setDivisions(divisions);
        mesh.setMinorRadius(minorRadius);
        mesh.setMajorRadius(majorRadius);
    }
    
    public Spheroid(Color c) {
        this();
        this.setDiffuseColor(c);
    }

    public Spheroid(double radius, Color c) {
        this(radius);
        this.setDiffuseColor(c);
    }

    public Spheroid(double minorRadius, double majorRadius, Color c) {
        this(minorRadius, majorRadius);
        this.setDiffuseColor(c);
    }
    
    public Spheroid(int divisions, double minorRadius, double majorRadius, Color c) {
        this(divisions, minorRadius, majorRadius);
        this.setDiffuseColor(c);
    }

    public final void setMajorRadius(double value) {
        mesh.setMajorRadius(value);
    }

    public final void setMinorRadius(double value) {
        mesh.setMinorRadius(value);
    }

    public final void setDivisions(int value) {
        mesh.setDivisions(value);
    }

    public final void setDrawMode(DrawMode value) {
        mesh.setDrawMode(value);
    }

    public final void setCullFace(CullFace value) {
        mesh.setCullFace(value);
    }

    public boolean isSphere() {
        return mesh.isSphere();
    }

    public boolean isOblateSpheroid() {
        return mesh.isOblateSpheroid();
    }

    public boolean isProlateSpheroid() {
        return mesh.isProlateSpheroid();
    }

    public final double getMajorRadius() {
        return mesh.getMajorRadius();
    }

    public DoubleProperty majorRadiusProperty() {
        return mesh.majorRadiusProperty();
    }

    public final double getMinorRadius() {
        return mesh.getMinorRadius();
    }

    public DoubleProperty minorRadiusProperty() {
        return mesh.minorRadiusProperty();
    }

    public final int getDivisions() {
        return mesh.getDivisions();
    }

    public IntegerProperty divisionsProperty() {
        return mesh.divisionsProperty();
    }
    
    
    
}
