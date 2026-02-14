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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TrapezoidMeshTest {

    private TrapezoidMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new TrapezoidMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getSmallSize(), is(30.0));
        assertThat(mesh.getBigSize(), is(50.0));
        assertThat(mesh.getHeight(), is(40.0));
        assertThat(mesh.getDepth(), is(60.0));
    }

    @Test
    @DisplayName("Parameterized constructor sets values correctly")
    public void testParameterizedConstruction() {
        mesh = new TrapezoidMesh(20.0, 40.0, 30.0, 50.0);

        assertThat(mesh.getSmallSize(), is(20.0));
        assertThat(mesh.getBigSize(), is(40.0));
        assertThat(mesh.getHeight(), is(30.0));
        assertThat(mesh.getDepth(), is(50.0));
    }

    @Test
    @DisplayName("Mesh is updated when smallSize changes")
    public void testMeshUpdatesOnSmallSizeChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setSmallSize(15.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when bigSize changes")
    public void testMeshUpdatesOnBigSizeChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setBigSize(100.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when height changes")
    public void testMeshUpdatesOnHeightChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setheight(80.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when depth changes")
    public void testMeshUpdatesOnDepthChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setDepth(100.0);

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
    @DisplayName("Trapezoid has exactly 8 vertices")
    public void testTrapezoidVertexCount() {
        TriangleMesh tm = (TriangleMesh) mesh.getMesh();

        // 8 vertices * 3 coordinates (x, y, z)
        assertThat(tm.getPoints().size(), is(24));
    }

    @Test
    @DisplayName("Trapezoid has exactly 12 faces")
    public void testTrapezoidFaceCount() {
        TriangleMesh tm = (TriangleMesh) mesh.getMesh();

        // 12 faces * 6 indices (3 vertex indices + 3 texture indices)
        assertThat(tm.getFaces().size(), is(72));
    }

    @Test
    @DisplayName("Property accessors work correctly")
    public void testPropertyAccessors() {
        assertThat(mesh.sizeSmallProperty(), is(notNullValue()));
        assertThat(mesh.sizeBigProperty(), is(notNullValue()));
        assertThat(mesh.heightProperty(), is(notNullValue()));
        assertThat(mesh.depthProperty(), is(notNullValue()));
    }
}
