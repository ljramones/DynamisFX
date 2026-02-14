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
import javafx.scene.DepthTest;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.shapes.primitives.helper.MeshProperty;

/**
 *
 * @author Moussaab AMRINE dy_amrine@esi.dz
 * @author  Yehya BELHAMRA dy_belhamra@esi.dz
 */

public class TrapezoidMesh extends TexturedMesh {

    private final static double DEFAULT_SMALLSIZE 	= 30 ;
    private final static double DEFAULT_BIGSIZE         = 50 ;
    private final static double DEFAULT_HEIGHT		= 40 ;
    private final static double DEFAULT_DEPTH 		= 60 ;


    public TrapezoidMesh (){
        this(DEFAULT_SMALLSIZE, DEFAULT_BIGSIZE, DEFAULT_HEIGHT, DEFAULT_DEPTH);
    }

    public TrapezoidMesh (double smallSize, double bigSize, double height, double depth){
        setSmallSize(smallSize);
        setBigSize(bigSize);
        setheight(height);
        setDepth(depth);
        setDepthTest(DepthTest.ENABLE);
        updateMesh();
    }

    @Override
    protected final void updateMesh() {
        setMesh(null);
        mesh = createTrapezoid((float)getSmallSize(), (float)getBigSize(), (float)getHeight(), (float)getDepth());
        setMesh(mesh);
    }

    private TriangleMesh createTrapezoid (double smallSize, double bigSize, double high, double depth){
        TriangleMesh m = new TriangleMesh();
        float s = ((float)smallSize) ;
        float b = ((float)bigSize);
        float h = ((float)high);
        float d = ((float)depth);

        //create Points
        m.getPoints().addAll(
                -s/2 , -h/2 ,  d/2,	// A = 0
                s/2 , -h/2 ,  d/2,	// B = 1
                -b/2 ,  h/2 ,  d/2,	// C = 2
                b/2 ,  h/2 ,  d/2,	// D = 3
                -s/2 , -h/2 , -d/2,	// E = 4
                s/2 , -h/2 , -d/2,	// F = 5
                -b/2 ,  h/2 , -d/2,	// G = 6
                b/2 ,  h/2 , -d/2	// H = 7
        );

        m.getTexCoords().addAll(0,0);

        m.getFaces().addAll(
                0 , 0 , 1 , 0 , 3 , 0 ,		// A-B-D
                0 , 0 , 3 , 0 , 2 , 0 , 	// A-D-C
                0 , 0 , 2 , 0 , 6 , 0 ,		// A-C-G
                0 , 0 , 6 , 0 , 4 , 0 , 	// A-G-E
                0 , 0 , 4 , 0 , 1 , 0 ,		// A-E-B
                1 , 0 , 4 , 0 , 5 , 0 , 	// B-E-F
                1 , 0 , 5 , 0 , 7 , 0 ,		// B-F-H
                1 , 0 , 7 , 0 , 3 , 0 ,		// B-H-D
                3 , 0 , 7 , 0 , 6 , 0 ,		// D-H-G
                3 , 0 , 6 , 0 , 2 , 0 ,		// D-G-C
                6 , 0 , 7 , 0 , 5 , 0 ,		// G-H-F
                6 , 0 , 5 , 0 , 4 , 0		// G-F-E
        );

        return m ;
    }


    private final DoubleProperty sizeSmall = MeshProperty.createDoubleUnguarded(
            DEFAULT_SMALLSIZE, this::updateMesh);

    public final double getSmallSize() {
        return sizeSmall.get();
    }

    public final void setSmallSize(double value) {
        sizeSmall.set(value);
    }

    public DoubleProperty sizeSmallProperty() {
        return sizeSmall;
    }

    private final DoubleProperty sizeBig = MeshProperty.createDoubleUnguarded(
            DEFAULT_BIGSIZE, this::updateMesh);

    public final double getBigSize() {
        return sizeBig.get();
    }

    public final void setBigSize(double value) {
        sizeBig.set(value);
    }

    public DoubleProperty sizeBigProperty() {
        return sizeBig;
    }


    private final DoubleProperty height = MeshProperty.createDoubleUnguarded(
            DEFAULT_HEIGHT, this::updateMesh);

    public final double getHeight() {
        return height.get();
    }

    public final void setheight(double value) {
        height.set(value);
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    private final DoubleProperty depth = MeshProperty.createDoubleUnguarded(
            DEFAULT_DEPTH, this::updateMesh);

    public final double getDepth() {
        return depth.get();
    }

    public final void setDepth(double value) {
        depth.set(value);
    }

    public DoubleProperty depthProperty() {
        return depth;
    }


}
