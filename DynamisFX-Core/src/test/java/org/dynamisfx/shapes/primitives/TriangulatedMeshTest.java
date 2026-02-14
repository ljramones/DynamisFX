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

import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.geometry.Point3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TriangulatedMeshTest {

    private TriangulatedMesh mesh;
    private List<Point3D> points;

    @BeforeEach
    public void setUp() {
        // Create a simple triangle
        points = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0),
            new Point3D(5, 10, 0)
        );
        mesh = new TriangulatedMesh(points);
    }

    @Test
    @DisplayName("Constructor with points uses default values")
    public void testPointsConstruction() {
        assertThat(mesh.getHeight(), is(1.0));
        assertThat(mesh.getLevel(), is(1));
        assertThat(mesh.getHoleRadius(), is(0.0));
    }

    @Test
    @DisplayName("Constructor with height sets value correctly")
    public void testHeightConstruction() {
        mesh = new TriangulatedMesh(points, 5.0);

        assertThat(mesh.getHeight(), is(5.0));
    }

    @Test
    @DisplayName("Mesh is updated when height changes")
    public void testMeshUpdatesOnHeightChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setHeight(10.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when level changes")
    public void testMeshUpdatesOnLevelChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setLevel(2);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh has vertices")
    public void testMeshHasVertices() {
        TriangleMesh tm = (TriangleMesh) mesh.getMesh();

        assertThat(tm.getPoints().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Mesh has faces")
    public void testMeshHasFaces() {
        TriangleMesh tm = (TriangleMesh) mesh.getMesh();

        assertThat(tm.getFaces().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Property accessors work correctly")
    public void testPropertyAccessors() {
        assertThat(mesh.heightProperty(), is(notNullValue()));
        assertThat(mesh.levelProperty(), is(notNullValue()));
        assertThat(mesh.holeRadiusProperty(), is(notNullValue()));
        assertThat(mesh.boundsProperty(), is(notNullValue()));
    }

    @Test
    @DisplayName("Square polygon creates valid mesh")
    public void testSquarePolygon() {
        List<Point3D> square = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0),
            new Point3D(10, 10, 0),
            new Point3D(0, 10, 0)
        );
        mesh = new TriangulatedMesh(square);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
        assertThat(tm.getFaces().size(), greaterThan(0));
    }
}
