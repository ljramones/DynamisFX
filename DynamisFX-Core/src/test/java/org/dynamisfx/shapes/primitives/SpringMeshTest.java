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

public class SpringMeshTest {

    private SpringMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new SpringMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getMeanRadius(), is(10.0));
        assertThat(mesh.getWireRadius(), is(0.2));
        assertThat(mesh.getPitch(), is(5.0));
        assertThat(mesh.getLength(), is(100.0));
        assertThat(mesh.getLengthDivisions(), is(200));
        assertThat(mesh.getWireDivisions(), is(50));
    }

    @Test
    @DisplayName("Four-parameter constructor sets values correctly")
    public void testFourParamConstruction() {
        mesh = new SpringMesh(15.0, 0.5, 10.0, 150.0);

        assertThat(mesh.getMeanRadius(), is(15.0));
        assertThat(mesh.getWireRadius(), is(0.5));
        assertThat(mesh.getPitch(), is(10.0));
        assertThat(mesh.getLength(), is(150.0));
    }

    @Test
    @DisplayName("Mesh is updated when mean radius changes")
    public void testMeshUpdatesOnMeanRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setMeanRadius(20.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when wire radius changes")
    public void testMeshUpdatesOnWireRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setWireRadius(1.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when pitch changes")
    public void testMeshUpdatesOnPitchChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setPitch(10.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when length changes")
    public void testMeshUpdatesOnLengthChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setLength(200.0);

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
        assertThat(mesh.meanRadiusProperty(), is(notNullValue()));
        assertThat(mesh.wireRadiusProperty(), is(notNullValue()));
        assertThat(mesh.pitchProperty(), is(notNullValue()));
        assertThat(mesh.lengthProperty(), is(notNullValue()));
        assertThat(mesh.lengthDivisionsProperty(), is(notNullValue()));
        assertThat(mesh.wireDivisionsProperty(), is(notNullValue()));
    }
}
