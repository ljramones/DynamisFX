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

public class IcosahedronMeshTest {

    private IcosahedronMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new IcosahedronMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getDiameter(), is(1.0f));
        assertThat(mesh.getLevel(), is(1));
    }

    @Test
    @DisplayName("Level-only constructor sets values correctly")
    public void testLevelConstruction() {
        mesh = new IcosahedronMesh(2);

        assertThat(mesh.getDiameter(), is(1.0f));
        assertThat(mesh.getLevel(), is(2));
    }

    @Test
    @DisplayName("Diameter-only constructor sets values correctly")
    public void testDiameterConstruction() {
        mesh = new IcosahedronMesh(2.0f);

        assertThat(mesh.getDiameter(), is(2.0f));
        assertThat(mesh.getLevel(), is(1));
    }

    @Test
    @DisplayName("Two-parameter constructor sets values correctly")
    public void testTwoParamConstruction() {
        mesh = new IcosahedronMesh(3.0f, 2);

        assertThat(mesh.getDiameter(), is(3.0f));
        assertThat(mesh.getLevel(), is(2));
    }

    @Test
    @DisplayName("Mesh is updated when diameter changes")
    public void testMeshUpdatesOnDiameterChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setDiameter(5.0f);

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
        assertThat(mesh.diameterProperty(), is(notNullValue()));
        assertThat(mesh.levelProperty(), is(notNullValue()));
    }

    @Test
    @DisplayName("Higher level produces more vertices")
    public void testHigherLevelMoreVertices() {
        IcosahedronMesh lowLevel = new IcosahedronMesh(1.0f, 1);
        IcosahedronMesh highLevel = new IcosahedronMesh(1.0f, 2);

        TriangleMesh lowMesh = (TriangleMesh) lowLevel.getMesh();
        TriangleMesh highMesh = (TriangleMesh) highLevel.getMesh();

        assertThat(highMesh.getPoints().size(), greaterThan(lowMesh.getPoints().size()));
    }
}
