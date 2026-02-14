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

public class SpheroidMeshTest {

    private SpheroidMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new SpheroidMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getMajorRadius(), is(50.0));
        assertThat(mesh.getMinorRadius(), is(12.0));
        assertThat(mesh.getDivisions(), is(64));
    }

    @Test
    @DisplayName("Single radius constructor creates sphere")
    public void testSingleRadiusConstruction() {
        mesh = new SpheroidMesh(25.0);

        assertThat(mesh.getMajorRadius(), is(25.0));
        assertThat(mesh.getMinorRadius(), is(25.0));
        assertThat(mesh.isSphere(), is(true));
    }

    @Test
    @DisplayName("Two-parameter constructor sets values correctly")
    public void testTwoParamConstruction() {
        mesh = new SpheroidMesh(30.0, 15.0);

        assertThat(mesh.getMajorRadius(), is(30.0));
        assertThat(mesh.getMinorRadius(), is(15.0));
    }

    @Test
    @DisplayName("Three-parameter constructor sets values correctly")
    public void testThreeParamConstruction() {
        mesh = new SpheroidMesh(32, 40.0, 20.0);

        assertThat(mesh.getDivisions(), is(32));
        assertThat(mesh.getMajorRadius(), is(40.0));
        assertThat(mesh.getMinorRadius(), is(20.0));
    }

    @Test
    @DisplayName("Mesh is updated when major radius changes")
    public void testMeshUpdatesOnMajorRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setMajorRadius(100.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when minor radius changes")
    public void testMeshUpdatesOnMinorRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setMinorRadius(30.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when divisions changes")
    public void testMeshUpdatesOnDivisionsChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setDivisions(32);

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
    @DisplayName("isSphere returns true when radii are equal")
    public void testIsSphere() {
        mesh = new SpheroidMesh(30.0);

        assertThat(mesh.isSphere(), is(true));
        assertThat(mesh.isOblateSpheroid(), is(false));
        assertThat(mesh.isProlateSpheroid(), is(false));
    }

    @Test
    @DisplayName("isOblateSpheroid returns true when major > minor")
    public void testIsOblateSpheroid() {
        mesh = new SpheroidMesh(50.0, 30.0);

        assertThat(mesh.isOblateSpheroid(), is(true));
        assertThat(mesh.isSphere(), is(false));
        assertThat(mesh.isProlateSpheroid(), is(false));
    }

    @Test
    @DisplayName("isProlateSpheroid returns true when major < minor")
    public void testIsProlateSpheroid() {
        mesh = new SpheroidMesh(30.0, 50.0);

        assertThat(mesh.isProlateSpheroid(), is(true));
        assertThat(mesh.isSphere(), is(false));
        assertThat(mesh.isOblateSpheroid(), is(false));
    }

    @Test
    @DisplayName("Property accessors work correctly")
    public void testPropertyAccessors() {
        assertThat(mesh.majorRadiusProperty(), is(notNullValue()));
        assertThat(mesh.minorRadiusProperty(), is(notNullValue()));
        assertThat(mesh.divisionsProperty(), is(notNullValue()));
    }
}
