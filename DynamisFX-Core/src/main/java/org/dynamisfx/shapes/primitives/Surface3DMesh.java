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

import org.dynamisfx.geometry.Face3;
import org.dynamisfx.shapes.primitives.helper.delaunay.DelaunayMesh;
import org.dynamisfx.shapes.primitives.helper.delaunay.Triangle3D;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.DepthTest;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.geometry.Point3D;

import java.util.ArrayList;
import java.util.List;

public class Surface3DMesh extends TexturedMesh {

    public Surface3DMesh() {
        this(new ArrayList<>());
    }

    public Surface3DMesh(List<Point3D> dataPoints) {
        setSurfaceData(dataPoints);

        updateMesh();
        setCullFace(CullFace.NONE);
        setDrawMode(DrawMode.LINE);
        setDepthTest(DepthTest.ENABLE);
    }

    // surfaceData
    private final ObjectProperty<List<Point3D>> surfaceData = new SimpleObjectProperty<>(this, "surfaceData") {
        @Override
        protected void invalidated() {
            if (mesh != null) {
                updateMesh();
            }
        }
    };
    public final ObjectProperty<List<Point3D>> surfaceDataProperty() {
        return surfaceData;
    }
    public final List<Point3D> getSurfaceData() {
        return surfaceData.get();
    }
    public final void setSurfaceData(List<Point3D> value) {
        surfaceData.set(value);
    }

    @Override
    protected void updateMesh() {
        setMesh(null);
        mesh=createPlotMesh();
        setMesh(mesh);
    }

    private TriangleMesh createPlotMesh() {
        listVertices.clear();
        listTextures.clear();
        listFaces.clear();

        DelaunayMesh delaunayMesh = new DelaunayMesh(getSurfaceData());

        listVertices.addAll(getSurfaceData());

        textureCoords = new float[2 * delaunayMesh.getNormalizedPoints().size()];
        int counter = 0;
        for (Point3D point : delaunayMesh.getNormalizedPoints()) {
            textureCoords[counter] = point.getX();
            textureCoords[counter++] = point.getY();
        }

        //add texture and face indices
        List<Point3D> normalizedPoints = delaunayMesh.getNormalizedPoints();
        for (Triangle3D triangle : delaunayMesh.getTriangle3DList()) {
            int faceIndex1 = normalizedPoints.indexOf(triangle.getP0());
            int faceIndex2 = normalizedPoints.indexOf(triangle.getP1());
            int faceIndex3 = normalizedPoints.indexOf(triangle.getP2());
            listTextures.add(new Face3(faceIndex1, faceIndex2, faceIndex3));
            listFaces.add(new Face3(faceIndex1, faceIndex2, faceIndex3));
        }

        return createMesh();
    }

}
