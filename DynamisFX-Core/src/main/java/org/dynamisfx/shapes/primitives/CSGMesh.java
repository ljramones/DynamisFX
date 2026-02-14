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

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Vertex;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.DepthTest;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.geometry.Face3;
import org.dynamisfx.geometry.Point3D;

/**
 *
 * @author José Pereda Llamas
 * Created on 01-may-2015 - 12:20:06
 */
public class CSGMesh extends TexturedMesh {
    
    private final CSG primitive;
    
    public CSGMesh(CSG primitive){
        this.primitive=primitive;
        
        updateMesh();
        setCullFace(CullFace.BACK);
        setDrawMode(DrawMode.FILL);
        setDepthTest(DepthTest.ENABLE);
    }

    @Override
    protected final void updateMesh() {
        setMesh(null);
        mesh=createCSGMesh();
        setMesh(mesh);
    }
    
    private TriangleMesh createCSGMesh(){
        List<Vertex> vertices = new ArrayList<>();
        List<List<Integer>> indices = new ArrayList<>();

        listVertices.clear();
        primitive.getPolygons().forEach(p -> {
            List<Integer> polyIndices = new ArrayList<>();
            
            p.vertices.forEach(v -> {
                if (!vertices.contains(v)) {
                    vertices.add(v);
                    listVertices.add(new Point3D((float)v.pos.getX(), (float)v.pos.getY(), (float)v.pos.getZ()));
                    polyIndices.add(vertices.size());
                } else {
                    polyIndices.add(vertices.indexOf(v) + 1);
                }
            });

            indices.add(polyIndices);
            
        });
        
        textureCoords=new float[]{0f,0f};
        listTextures.clear();
        listFaces.clear();
        indices.forEach(pVerts-> {
            int index1 = pVerts.get(0);
            for (int i = 0; i < pVerts.size() - 2; i++) {
                int index2 = pVerts.get(i + 1);
                int index3 = pVerts.get(i + 2);

                listTextures.add(new Face3(0, 0, 0));
                listFaces.add(new Face3(index1-1, index2-1, index3-1));
            }
        });
        int[] faceSmoothingGroups = new int[listFaces.size()];
        smoothingGroups=faceSmoothingGroups;
        
        return createMesh();
    }
}
