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

package org.dynamisfx.shapes.polygon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PolygonMeshTest {

    private PolygonMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new PolygonMesh();
    }

    @Test
    @DisplayName("Default constructor creates empty mesh")
    public void testDefaultConstruction() {
        assertThat(mesh.getPoints(), is(notNullValue()));
        assertThat(mesh.getTexCoords(), is(notNullValue()));
        assertThat(mesh.getFaces(), is(notNullValue()));
        assertThat(mesh.getPoints().size(), is(0));
    }

    @Test
    @DisplayName("Constructor with arrays sets data")
    public void testArrayConstruction() {
        float[] points = {0, 0, 0, 1, 0, 0, 0, 1, 0};
        float[] texCoords = {0, 0, 1, 0, 0, 1};
        int[][] faces = {{0, 0, 1, 1, 2, 2}};

        mesh = new PolygonMesh(points, texCoords, faces);

        assertThat(mesh.getPoints().size(), is(9));
        assertThat(mesh.getTexCoords().size(), is(6));
        assertThat(mesh.getFaces().length, is(1));
    }

    @Test
    @DisplayName("getPointElementSize returns 3")
    public void testPointElementSize() {
        assertThat(mesh.getPointElementSize(), is(3));
    }

    @Test
    @DisplayName("getTexCoordElementSize returns 2")
    public void testTexCoordElementSize() {
        assertThat(mesh.getTexCoordElementSize(), is(2));
    }

    @Test
    @DisplayName("getFaceElementSize returns 6")
    public void testFaceElementSize() {
        assertThat(mesh.getFaceElementSize(), is(6));
    }

    @Test
    @DisplayName("getFaceSmoothingGroups returns observable array")
    public void testFaceSmoothingGroups() {
        assertThat(mesh.getFaceSmoothingGroups(), is(notNullValue()));
    }

    @Test
    @DisplayName("setFaces updates faces")
    public void testSetFaces() {
        int[][] newFaces = {{0, 0, 1, 1, 2, 2}, {1, 1, 2, 2, 3, 3}};
        mesh.setFaces(newFaces);

        assertThat(mesh.getFaces().length, is(2));
    }

    @Test
    @DisplayName("getNumEdgesInFaces calculates correctly")
    public void testNumEdgesInFaces() {
        int[][] faces = {{0, 0, 1, 1, 2, 2}, {0, 0, 2, 2, 3, 3}};
        mesh.setFaces(faces);

        // Each face has 3 edges (triangle), total 6 edges, but shared so 6/2 = 3?
        // Actually each face[].length = 6, which is 3 point/tex pairs
        // So numEdges = (6 + 6) / 2 = 6
        assertThat(mesh.getNumEdgesInFaces(), is(6));
    }
}
