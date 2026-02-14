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

public class KnotMeshTest {

    private KnotMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new KnotMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getMajorRadius(), is(2.0));
        assertThat(mesh.getMinorRadius(), is(1.0));
        assertThat(mesh.getWireRadius(), is(0.2));
        assertThat(mesh.getP(), is(2.0));
        assertThat(mesh.getQ(), is(3.0));
        assertThat(mesh.getLengthDivisions(), is(200));
        assertThat(mesh.getWireDivisions(), is(50));
    }

    @Test
    @DisplayName("Five-parameter constructor sets values correctly")
    public void testFiveParamConstruction() {
        mesh = new KnotMesh(3.0, 1.5, 0.3, 3.0, 4.0);

        assertThat(mesh.getMajorRadius(), is(3.0));
        assertThat(mesh.getMinorRadius(), is(1.5));
        assertThat(mesh.getWireRadius(), is(0.3));
        assertThat(mesh.getP(), is(3.0));
        assertThat(mesh.getQ(), is(4.0));
    }

    @Test
    @DisplayName("Mesh is updated when major radius changes")
    public void testMeshUpdatesOnMajorRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setMajorRadius(4.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when minor radius changes")
    public void testMeshUpdatesOnMinorRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setMinorRadius(2.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when wire radius changes")
    public void testMeshUpdatesOnWireRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setWireRadius(0.5);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when p changes")
    public void testMeshUpdatesOnPChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setP(4.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when q changes")
    public void testMeshUpdatesOnQChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setQ(5.0);

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
        assertThat(mesh.majorRadiusProperty(), is(notNullValue()));
        assertThat(mesh.minorRadiusProperty(), is(notNullValue()));
        assertThat(mesh.wireRadiusProperty(), is(notNullValue()));
        assertThat(mesh.pProperty(), is(notNullValue()));
        assertThat(mesh.qProperty(), is(notNullValue()));
        assertThat(mesh.lengthDivisionsProperty(), is(notNullValue()));
        assertThat(mesh.wireDivisionsProperty(), is(notNullValue()));
    }
}
