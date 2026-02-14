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

package org.dynamisfx.shapes.primitives;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Point3D;
import org.dynamisfx.shapes.primitives.helper.MeshProperty;
import javafx.scene.DepthTest;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Translate;
import org.dynamisfx.geometry.Face3;

/**
 *
 * @author Sean
 */
public class SegmentedDomeMesh extends TexturedMesh {
    /*
        Field vars
    */
    private static final double DEFAULT_RADIUS = 50.0D;
    private static final double DEFAULT_PHIMIN = Math.toRadians(0);
    private static final double DEFAULT_PHIMAX = Math.toRadians(360);
    private static final double DEFAULT_THETAMIN = Math.toRadians(0);
    private static final double DEFAULT_THETAMAX = Math.toRadians(90);
    private static final int DEFAULT_DIVISIONS = 20;    
    
    public SegmentedDomeMesh() {
        this(DEFAULT_RADIUS, DEFAULT_PHIMIN, DEFAULT_PHIMAX, 
            DEFAULT_THETAMIN, DEFAULT_THETAMAX, DEFAULT_DIVISIONS);
    }
    
    /**
     * @param radius radius of the sphere segment
     * @param phimin The starting azimutal angle [rad], 0-2*pi.
     * @param phimax The ending azimutal angle [rad], 0-2*pi, phimax &gt;
     * phimin.
     * @param thetamin The starting polar angle [rad], -pi/2-pi/2.
     * @param thetamax The ending polar angle [rad], -pi/2-pi/2, thetamax &gt;
     * thetamin.
     * @param divisions The number of segments of curves approximations,
     * granulariy &gt; 2.
     */
    public SegmentedDomeMesh(double radius,
            double phimin, double phimax, 
            double thetamin, double thetamax,
            int divisions) {

        setRadius(radius);
        setPhimin(phimin);
        setPhimax(phimax);
        setThetamin(thetamin);
        setThetamax(thetamax);
        setDivisions(divisions);
        updateMesh();
        setDepthTest(DepthTest.ENABLE);
        
    }
    private TriangleMesh createSegmentedDome(double radius,
            double phimin, double phimax, 
            double thetamin, double thetamax,
            int divisions){
        
        listVertices.clear();
        listTextures.clear();
        listFaces.clear();   

        // Create Points
        double phi = phimin;
        double theta;

        for (int i = 0; i < divisions + 1; i++) {
            theta = thetamin;
            for (int j = 0; j < divisions + 1; j++) {
               org.dynamisfx.geometry.Point3D ta = new org.dynamisfx.geometry.Point3D(
                        (float) (radius * Math.cos(theta) * Math.sin(phi)),
                        (float) (radius * Math.cos(theta) * Math.cos(phi)),
                        (float) (radius * Math.sin(theta)));
                listVertices.add(ta);                
                theta += (thetamax - thetamin) / divisions;
            }
            phi += (phimax - phimin) / divisions;
        }        
        
        // Create texture coordinates
        createTexCoords(divisions, divisions);
        
        //Add the faces "winding" the points generally counter clock wise
        for (int i = 0; i < divisions; i++) {
            int multiplier = (i * divisions) + i;
            //Up the Outside
            for (int j = multiplier; j < divisions + multiplier; j++) {
                listFaces.add(new Face3(j, j + 1, j + divisions + 1)); //lower triangle
                listTextures.add(new Face3(j, j + 1, j + divisions + 1));
                listFaces.add(new Face3(j + divisions + 1, j + 1, j + divisions + 2)); //upper triangle
                listTextures.add(new Face3(j + divisions + 1, j + 1, j + divisions + 2));
            }
            //Down the Inside            
            for (int j = divisions + multiplier; j > multiplier; j--) {
                listFaces.add(new Face3(j, j - 1, j + divisions + 1)); ; //lower triangle
                listTextures.add(new Face3(j, j - 1, j + divisions + 1));
                listFaces.add(new Face3(j - 1, j + divisions, j + divisions + 1)); //upper triangle
                listTextures.add(new Face3(j - 1, j + divisions, j + divisions + 1));
            }
        }    
        
        return createMesh();
    }
    /*
        Properties
    */
    private final DoubleProperty radius = MeshProperty.createDoubleUnguarded(
            DEFAULT_RADIUS, this::updateMesh);

    public final double getRadius() {
        return radius.get();
    }

    public final void setRadius(double value) {
        radius.set(value);
    }

    public DoubleProperty radiusProperty() {
        return radius;
    }

    private final DoubleProperty phimin = MeshProperty.createDoubleUnguarded(
            DEFAULT_PHIMIN, this::updateMesh);

    public final double getPhimin() {
        return phimin.get();
    }

    public final void setPhimin(double value) {
        phimin.set(value);
    }

    public DoubleProperty phiminProperty() {
        return phimin;
    }

    private final DoubleProperty phimax = MeshProperty.createDoubleUnguarded(
            DEFAULT_PHIMAX, this::updateMesh);

    public final double getPhimax() {
        return phimax.get();
    }

    public final void setPhimax(double value) {
        phimax.set(value);
    }

    public DoubleProperty phimaxProperty() {
        return phimax;
    }

    private final DoubleProperty thetamax = MeshProperty.createDoubleUnguarded(
            DEFAULT_THETAMAX, this::updateMesh);

    public final double getThetamax() {
        return thetamax.get();
    }

    public final void setThetamax(double value) {
        thetamax.set(value);
    }

    public DoubleProperty thetamaxProperty() {
        return thetamax;
    }

    private final DoubleProperty thetamin = MeshProperty.createDoubleUnguarded(
            DEFAULT_THETAMIN, this::updateMesh);

    public final double getThetamin() {
        return thetamin.get();
    }

    public final void setThetamin(double value) {
        thetamin.set(value);
    }

    public DoubleProperty thetaminProperty() {
        return thetamin;
    }

    private final IntegerProperty divisions = MeshProperty.createIntegerUnguarded(
            DEFAULT_DIVISIONS, this::updateMesh);

    public final int getDivisions() {
        return divisions.get();
    }

    public final void setDivisions(int value) {
        divisions.set(value);
    }

    public IntegerProperty divisionsProperty() {
        return divisions;
    }        
    
    @Override
    protected final void updateMesh() {
        setMesh(null);
        mesh = createSegmentedDome(getRadius(), getPhimin(), getPhimax(), getThetamin(), getThetamax(), getDivisions());
        setMesh(mesh);
    }
}
