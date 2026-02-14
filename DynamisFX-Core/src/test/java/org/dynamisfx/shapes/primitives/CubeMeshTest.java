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

public class CubeMeshTest {

    private CubeMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new CubeMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getSize(), is(10.0));
        assertThat(mesh.getImagePadding(), is(0.0015f));
    }

    @Test
    @DisplayName("Parameterized constructor sets size correctly")
    public void testParameterizedConstruction() {
        mesh = new CubeMesh(25.0);

        assertThat(mesh.getSize(), is(25.0));
    }

    @Test
    @DisplayName("Mesh is updated when size changes")
    public void testMeshUpdatesOnSizeChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setSize(50.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Image padding can be changed")
    public void testImagePaddingChange() {
        mesh.setImagePadding(0.005f);

        assertThat(mesh.getImagePadding(), is(0.005f));
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
        assertThat(mesh.sizeProperty(), is(notNullValue()));
        assertThat(mesh.imagePaddingProperty(), is(notNullValue()));
    }
}
