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

public class SegmentedSphereMeshTest {

    private SegmentedSphereMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new SegmentedSphereMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getRadiusDivisions(), is(64));
        assertThat(mesh.getRadiusCropX(), is(0));
        assertThat(mesh.getRadiusCropY(), is(0));
        assertThat(mesh.getRadius(), is(5.0));
    }

    @Test
    @DisplayName("Radius-only constructor sets value correctly")
    public void testRadiusConstruction() {
        mesh = new SegmentedSphereMesh(10.0);

        assertThat(mesh.getRadius(), is(10.0));
    }

    @Test
    @DisplayName("Mesh is updated when radius changes")
    public void testMeshUpdatesOnRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setRadius(15.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when divisions changes")
    public void testMeshUpdatesOnDivisionsChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setRadiusDivisions(32);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when crop X changes")
    public void testMeshUpdatesOnCropXChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setRadiusCropX(5);

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
        assertThat(mesh.radiusProperty(), is(notNullValue()));
        assertThat(mesh.radiusDivisionsProperty(), is(notNullValue()));
        assertThat(mesh.radiusCropXProperty(), is(notNullValue()));
        assertThat(mesh.radiusCropYProperty(), is(notNullValue()));
        assertThat(mesh.centerProperty(), is(notNullValue()));
    }
}
