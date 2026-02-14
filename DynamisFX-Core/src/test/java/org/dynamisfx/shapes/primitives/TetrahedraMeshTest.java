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
import org.dynamisfx.geometry.Point3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TetrahedraMeshTest {

    private TetrahedraMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new TetrahedraMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getHeight(), is(10.0));
        assertThat(mesh.getLevel(), is(1));
    }

    @Test
    @DisplayName("Height constructor sets height correctly")
    public void testHeightConstruction() {
        mesh = new TetrahedraMesh(20.0);

        assertThat(mesh.getHeight(), is(20.0));
        assertThat(mesh.getLevel(), is(1));
    }

    @Test
    @DisplayName("Full constructor sets all values correctly")
    public void testFullConstruction() {
        Point3D center = new Point3D(5, 5, 5);
        mesh = new TetrahedraMesh(15.0, 2, center);

        assertThat(mesh.getHeight(), is(15.0));
        assertThat(mesh.getLevel(), is(2));
        assertThat(mesh.getCenter(), is(center));
    }

    @Test
    @DisplayName("Mesh is updated when height changes")
    public void testMeshUpdatesOnHeightChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setHeight(30.0);

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
    @DisplayName("Mesh is updated when center changes")
    public void testMeshUpdatesOnCenterChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setCenter(new Point3D(10, 10, 10));

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
        assertThat(mesh.heightProperty(), is(notNullValue()));
        assertThat(mesh.levelProperty(), is(notNullValue()));
        assertThat(mesh.centerProperty(), is(notNullValue()));
    }

    @Test
    @DisplayName("Higher level creates more faces")
    public void testLevelIncreasesComplexity() {
        mesh = new TetrahedraMesh(10.0, 1, null);
        TriangleMesh level1Mesh = (TriangleMesh) mesh.getMesh();
        int level1Faces = level1Mesh.getFaces().size();

        mesh = new TetrahedraMesh(10.0, 2, null);
        TriangleMesh level2Mesh = (TriangleMesh) mesh.getMesh();
        int level2Faces = level2Mesh.getFaces().size();

        assertThat(level2Faces, greaterThan(level1Faces));
    }
}
