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

package org.dynamisfx.shapes.composites;

import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 *
 * @author Sean
 */
public class SurfacePlot extends Group {

    public AmbientLight selfLight = new AmbientLight(Color.WHITE);
    public double nodeRadius = 1;
    private double axesSize = 1000;
    private boolean normalized = false;
    public boolean selfLightEnabled = true;
    public Color color = Color.WHITE;
    private TriangleMesh mesh;
    public MeshView meshView;
    public PhongMaterial material;

    public SurfacePlot(boolean selfLit) {
        selfLightEnabled = selfLit;
        init();
    }

    public SurfacePlot(float[][] arrayY, int spacing, Color color, boolean fill, boolean selfLit) {
        selfLightEnabled = selfLit;
        init();
        setHeightData(arrayY,spacing, color,selfLit,fill);
    }

    private void init() {
        if (selfLightEnabled) {
            getChildren().add(selfLight);
        }
        setDepthTest(DepthTest.ENABLE);
    }

    public void setHeightData(float[][] arrayY, int spacing, Color color, boolean ambient, boolean fill) {
        material = new PhongMaterial();
        material.setSpecularColor(color);
        material.setDiffuseColor(color);

        mesh = new TriangleMesh();

        // Fill Points
        for (int x = 0; x < arrayY.length; x++) {
            for (int z = 0; z < arrayY[0].length; z++) {
                mesh.getPoints().addAll(x * spacing, arrayY[x][z], z * spacing);
            }
        }

        //for now we'll just make an empty texCoordinate group
        mesh.getTexCoords().addAll(0, 0);
        int total = arrayY.length * arrayY.length;
        int nextRow = arrayY.length;
        //Add the faces "winding" the points generally counter clock wise
        for (int i = 0; i < total - nextRow -1; i++) {
            //Top upper left triangle
            mesh.getFaces().addAll(i,0,i+nextRow,0,i+1,0);
            //Top lower right triangle
            mesh.getFaces().addAll(i+nextRow,0,i+nextRow + 1,0,i+1,0);
            
            //Bottom            
        }
        //Create a viewable MeshView to be added to the scene
        //To add a TriangleMesh to a 3D scene you need a MeshView container object
        meshView = new MeshView(mesh);
        //The MeshView allows you to control how the TriangleMesh is rendered
        if(fill) { 
            meshView.setDrawMode(DrawMode.FILL);
        } else {
            meshView.setDrawMode(DrawMode.LINE); //show lines only by default
        }
        meshView.setCullFace(CullFace.BACK); //Removing culling to show back lines

        getChildren().add(meshView);
        meshView.setMaterial(material);
        if (ambient) {
            selfLight.getScope().add(meshView);
            if(!getChildren().contains(selfLight))
                getChildren().add(selfLight);
        }
        else if(getChildren().contains(selfLight))
            getChildren().remove(selfLight);
        setDepthTest(DepthTest.ENABLE);
    }
}