/**
 * TrapezoidMesh.java
 *
 * Copyright (c) 2013-2016, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.fxyz3d.shapes.primitives;

import javafx.beans.property.DoubleProperty;
import javafx.scene.DepthTest;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.shapes.primitives.helper.MeshProperty;

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
