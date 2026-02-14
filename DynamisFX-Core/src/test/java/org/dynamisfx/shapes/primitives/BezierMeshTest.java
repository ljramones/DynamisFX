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
import org.dynamisfx.shapes.primitives.helper.BezierHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BezierMeshTest {

    private BezierMesh mesh;
    private BezierHelper spline;

    @BeforeEach
    public void setUp() {
        Point3D a = new Point3D(0, 0, 0);
        Point3D b = new Point3D(1, 1, 0);
        Point3D c = new Point3D(2, 0, 0);
        Point3D d = new Point3D(3, 1, 0);
        spline = new BezierHelper(a, b, c, d);
        mesh = new BezierMesh(spline);
    }

    @Test
    @DisplayName("Constructor with spline sets default wire radius")
    public void testSplineConstruction() {
        assertThat(mesh.getWireRadius(), is(0.2));
        assertThat(mesh.getLengthDivisions(), is(200));
        assertThat(mesh.getWireDivisions(), is(50));
    }

    @Test
    @DisplayName("Constructor with wire radius sets value correctly")
    public void testWireRadiusConstruction() {
        mesh = new BezierMesh(spline, 0.5);

        assertThat(mesh.getWireRadius(), is(0.5));
    }

    @Test
    @DisplayName("Mesh is updated when wire radius changes")
    public void testMeshUpdatesOnWireRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setWireRadius(0.5);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when length divisions changes")
    public void testMeshUpdatesOnLengthDivisionsChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setLengthDivisions(100);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when wire divisions changes")
    public void testMeshUpdatesOnWireDivisionsChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setWireDivisions(25);

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
        assertThat(mesh.splineProperty(), is(notNullValue()));
        assertThat(mesh.wireRadiusProperty(), is(notNullValue()));
        assertThat(mesh.lengthDivisionsProperty(), is(notNullValue()));
        assertThat(mesh.wireDivisionsProperty(), is(notNullValue()));
        assertThat(mesh.lengthCropProperty(), is(notNullValue()));
        assertThat(mesh.wireCropProperty(), is(notNullValue()));
    }
}
