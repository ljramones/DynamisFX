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

public class PrismMeshTest {

    private PrismMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new PrismMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getRadius(), is(1.0));
        assertThat(mesh.getHeight(), is(10.0));
        assertThat(mesh.getLevel(), is(1));
    }

    @Test
    @DisplayName("Two-parameter constructor sets values correctly")
    public void testTwoParamConstruction() {
        mesh = new PrismMesh(2.0, 15.0);

        assertThat(mesh.getRadius(), is(2.0));
        assertThat(mesh.getHeight(), is(15.0));
    }

    @Test
    @DisplayName("Three-parameter constructor sets values correctly")
    public void testThreeParamConstruction() {
        mesh = new PrismMesh(3.0, 20.0, 2);

        assertThat(mesh.getRadius(), is(3.0));
        assertThat(mesh.getHeight(), is(20.0));
        assertThat(mesh.getLevel(), is(2));
    }

    @Test
    @DisplayName("Mesh is updated when radius changes")
    public void testMeshUpdatesOnRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setRadius(5.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when height changes")
    public void testMeshUpdatesOnHeightChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setHeight(20.0);

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
    @DisplayName("Mesh has texture coordinates")
    public void testMeshHasTexCoords() {
        TriangleMesh tm = (TriangleMesh) mesh.getMesh();

        assertThat(tm.getTexCoords().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Property accessors work correctly")
    public void testPropertyAccessors() {
        assertThat(mesh.radiusProperty(), is(notNullValue()));
        assertThat(mesh.heightProperty(), is(notNullValue()));
        assertThat(mesh.levelProperty(), is(notNullValue()));
        assertThat(mesh.axisOriginProperty(), is(notNullValue()));
        assertThat(mesh.axisEndProperty(), is(notNullValue()));
    }
}
