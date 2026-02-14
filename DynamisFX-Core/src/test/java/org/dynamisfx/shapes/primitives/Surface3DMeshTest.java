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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Surface3DMeshTest {

    private Surface3DMesh mesh;
    private List<Point3D> surfaceData;

    @BeforeEach
    public void setUp() {
        // Create a simple set of 3D points for Delaunay triangulation
        surfaceData = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0),
            new Point3D(5, 10, 0),
            new Point3D(0, 10, 5),
            new Point3D(10, 10, 5)
        );
        mesh = new Surface3DMesh(surfaceData);
    }

    @Test
    @DisplayName("Default constructor creates mesh with empty data")
    public void testDefaultConstruction() {
        mesh = new Surface3DMesh();

        assertThat(mesh.getSurfaceData(), is(notNullValue()));
    }

    @Test
    @DisplayName("Constructor with data sets surface data correctly")
    public void testDataConstruction() {
        assertThat(mesh.getSurfaceData(), is(surfaceData));
    }

    @Test
    @DisplayName("Mesh is updated when surface data changes")
    public void testMeshUpdatesOnSurfaceDataChange() {
        Mesh oldMesh = mesh.getMesh();

        List<Point3D> newData = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(20, 0, 0),
            new Point3D(10, 20, 0),
            new Point3D(0, 20, 10)
        );
        mesh.setSurfaceData(newData);

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
    @DisplayName("Property accessor works correctly")
    public void testPropertyAccessor() {
        assertThat(mesh.surfaceDataProperty(), is(notNullValue()));
    }
}
