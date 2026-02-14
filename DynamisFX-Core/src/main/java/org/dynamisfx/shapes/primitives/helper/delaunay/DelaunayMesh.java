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

package org.dynamisfx.shapes.primitives.helper.delaunay;

import org.dynamisfx.shapes.primitives.helper.delaunay.jdt.DelaunayTriangulation;
import org.dynamisfx.shapes.primitives.helper.delaunay.jdt.Point;
import org.dynamisfx.shapes.primitives.helper.delaunay.jdt.Triangle;
import org.dynamisfx.geometry.Point3D;
import org.dynamisfx.utils.DataBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DelaunayMesh {

    private final List<Point3D> dataPoints;
    private final List<Point3D> normalizedPoints;
    private final List<Triangle3D> triangle3DList;

    private final DataBox dataBox;
    private final DelaunayPointAdapter adapter;

    public DelaunayMesh(List<Point3D> dataPoints) {
        this.dataPoints = dataPoints;
        dataBox = new DataBox(dataPoints);

        // normalize points according to coordinate system size
        normalizedPoints = new ArrayList<>(dataPoints.size());
        for (Point3D point : dataPoints) {
            double x = (point.getX() - dataBox.getMinX()) / dataBox.getSizeX();
            double y = (point.getY() - dataBox.getMinY()) / dataBox.getSizeY();
            double z = (point.getZ() - dataBox.getMinZ()) / dataBox.getSizeZ();
            normalizedPoints.add(new Point3D(x, y, z));
        }

        triangle3DList = new ArrayList<>(dataPoints.size() / 2);

        adapter = new DelaunayPointAdapter();

        // convert input for Delaunay algorithm
        List<Point> normalizedOldPoints = normalizedPoints.stream()
                .map(adapter::convertPoint3DtoDelaunay)
                .collect(Collectors.toList());

        // Do Delaunay triangulation
        List<Triangle> triangulation = new DelaunayTriangulation(normalizedOldPoints).getTriangulation();

        // convert output of Delaunay algorithm back to triangle objects
        triangle3DList.addAll(triangulation.stream()
                .filter(triangle -> ! triangle.isHalfplane())
                .map(t -> Triangle3D.of(
                        adapter.convertPointFromDelaunay(t.getA()),
                        adapter.convertPointFromDelaunay(t.getB()),
                        adapter.convertPointFromDelaunay(t.getC())))
                .collect(Collectors.toList()));
    }

    public List<Point3D> getDataPoints() {
        return dataPoints;
    }

    public List<Point3D> getNormalizedPoints() {
        return normalizedPoints;
    }

    public List<Triangle3D> getTriangle3DList() {
        return triangle3DList;
    }
}
