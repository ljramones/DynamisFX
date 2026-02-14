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
 */

package org.dynamisfx.shapes.primitives;

import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TetrahedronMeshTest {

    private TetrahedronMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new TetrahedronMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getHeight(), is(100.0));
    }

    @Test
    @DisplayName("Parameterized constructor sets height correctly")
    public void testParameterizedConstruction() {
        mesh = new TetrahedronMesh(50.0);

        assertThat(mesh.getHeight(), is(50.0));
    }

    @Test
    @DisplayName("Mesh is updated when height changes")
    public void testMeshUpdatesOnHeightChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setHeight(200.0);

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
    @DisplayName("Tetrahedron has exactly 4 vertices")
    public void testTetrahedronVertexCount() {
        TriangleMesh tm = (TriangleMesh) mesh.getMesh();

        // 4 vertices * 3 coordinates (x, y, z)
        assertThat(tm.getPoints().size(), is(12));
    }

    @Test
    @DisplayName("Tetrahedron has exactly 4 faces")
    public void testTetrahedronFaceCount() {
        TriangleMesh tm = (TriangleMesh) mesh.getMesh();

        // 4 faces * 6 indices (3 vertex indices + 3 texture indices)
        assertThat(tm.getFaces().size(), is(24));
    }

    @Test
    @DisplayName("Property accessor works correctly")
    public void testPropertyAccessor() {
        assertThat(mesh.heightProperty(), is(notNullValue()));
    }
}
