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
import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.Sphere;
import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CSGMeshTest {

    @Test
    @DisplayName("CSGMesh from cube has vertices")
    public void testCubeHasVertices() {
        CSG cube = new Cube(10).toCSG();
        CSGMesh mesh = new CSGMesh(cube);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
    }

    @Test
    @DisplayName("CSGMesh from cube has faces")
    public void testCubeHasFaces() {
        CSG cube = new Cube(10).toCSG();
        CSGMesh mesh = new CSGMesh(cube);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getFaces().size(), greaterThan(0));
    }

    @Test
    @DisplayName("CSGMesh from sphere has vertices")
    public void testSphereHasVertices() {
        CSG sphere = new Sphere(5).toCSG();
        CSGMesh mesh = new CSGMesh(sphere);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
    }

    @Test
    @DisplayName("CSGMesh from union operation works")
    public void testUnionOperation() {
        CSG cube = new Cube(10).toCSG();
        CSG sphere = new Sphere(5).toCSG().transformed(eu.mihosoft.vvecmath.Transform.unity().translate(5, 0, 0));
        CSG union = cube.union(sphere);
        CSGMesh mesh = new CSGMesh(union);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
        assertThat(tm.getFaces().size(), greaterThan(0));
    }

    @Test
    @DisplayName("CSGMesh from difference operation works")
    public void testDifferenceOperation() {
        CSG cube = new Cube(10).toCSG();
        CSG sphere = new Sphere(5).toCSG();
        CSG difference = cube.difference(sphere);
        CSGMesh mesh = new CSGMesh(difference);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
        assertThat(tm.getFaces().size(), greaterThan(0));
    }

    @Test
    @DisplayName("CSGMesh from intersect operation works")
    public void testIntersectOperation() {
        CSG cube = new Cube(10).toCSG();
        CSG sphere = new Sphere(7).toCSG();
        CSG intersect = cube.intersect(sphere);
        CSGMesh mesh = new CSGMesh(intersect);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
        assertThat(tm.getFaces().size(), greaterThan(0));
    }
}
