/**
 * ConeMesh.java
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

package org.dynamisfx.shapes.primitives;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.shapes.primitives.helper.MeshProperty;

/**
 * A 3D cone mesh with configurable radius, height, and divisions.
 * <p>
 * The cone is oriented with the apex at the origin (y=0) and the base
 * extending downward along the positive Y axis.
 * <p>
 * <b>Texture Support:</b> This mesh uses a single texture coordinate and
 * supports solid color rendering via {@link javafx.scene.paint.PhongMaterial}.
 * Image texture mapping is not currently implemented (would require proper
 * UV coordinates for each vertex).
 * <p>
 * Example usage:
 * <pre>{@code
 * ConeMesh cone = new ConeMesh(25.0, 50.0);  // radius=25, height=50
 * cone.setMaterial(new PhongMaterial(Color.RED));
 * }</pre>
 *
 * @author Birdasaur (adapted from Dub's CapsuleMesh)
 * @see CapsuleMesh
 */
public class ConeMesh extends TexturedMesh {
    /*
        Field vars
    */
    private static final int DEFAULT_DIVISIONS = 32;    
    private static final double DEFAULT_RADIUS = 25.0D;
    private static final double DEFAULT_HEIGHT = 50.0D;
    
    /*
    Constructors
     */
    public ConeMesh() {
        this(DEFAULT_DIVISIONS, DEFAULT_RADIUS, DEFAULT_HEIGHT);
    }
    
    public ConeMesh(double radius, double height){
        this(DEFAULT_DIVISIONS, radius, height);
    }
    
    public ConeMesh(int divisions, double radius, double height) {    
        setDivisions(divisions);
        setRadius(radius);
        setHeight(height);    
        setMesh(createCone(getDivisions(), (float)getRadius(), (float)getHeight()));        
    }

    /*
    Methods
     */
    private TriangleMesh createCone(int divisions, float radius, float height) {
        mesh = new TriangleMesh();
        //Start with the top of the cone, later we will build our faces from these
        mesh.getPoints().addAll(0,0,0); //Point 0: Top of the Cone        
        //Generate the segments of the bottom circle (Cone Base)
        double segment_angle = 2.0 * Math.PI / divisions;
        float x, z;
        double angle;
        double halfCount = (Math.PI / 2 - Math.PI / (divisions / 2)); 
        // Reverse loop for speed!! der
        for(int i=divisions+1;--i >= 0; ) {
            angle = segment_angle * i;
            x = (float)(radius * Math.cos(angle - halfCount));
            z = (float)(radius * Math.sin(angle - halfCount));
            mesh.getPoints().addAll(x,height,z); 
        }   
        mesh.getPoints().addAll(0,height,0); //Point N: Center of the Cone Base

        // Single texture coordinate - supports solid color rendering.
        // Note: Image texture mapping requires proper UV coordinates per vertex.
        mesh.getTexCoords().addAll(0,0);

        // Add faces winding counter-clockwise (viewed from outside)
        for(int i=1;i<=divisions;i++) {
            mesh.getFaces().addAll(
                0,0,i+1,0,i,0,           // Vertical faces (cone surface)
                divisions+2,0,i,0,i+1,0   // Base faces (cone bottom)
            );
        }
        return mesh;
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

    private final DoubleProperty height = MeshProperty.createDoubleUnguarded(
            DEFAULT_HEIGHT, this::updateMesh);

    public final double getHeight() {
        return height.get();
    }

    public final void setHeight(double value) {
        height.set(value);
    }

    public DoubleProperty heightProperty() {
        return height;
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
    protected void updateMesh() {
        setMesh(null);
        mesh = createCone(getDivisions(), (float)getRadius(), (float)getHeight());
        setMesh(mesh);
    }
}