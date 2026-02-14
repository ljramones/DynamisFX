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

import javafx.geometry.Point2D;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SurfacePlotMeshTest {

    private SurfacePlotMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new SurfacePlotMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getRangeX(), is(10.0));
        assertThat(mesh.getRangeY(), is(10.0));
        assertThat(mesh.getDivisionsX(), is(64));
        assertThat(mesh.getDivisionsY(), is(64));
        assertThat(mesh.getFunctionScale(), is(1.0));
    }

    @Test
    @DisplayName("Function constructor sets function correctly")
    public void testFunctionConstruction() {
        Function<Point2D, Number> func = p -> p.getX() + p.getY();
        mesh = new SurfacePlotMesh(func);

        assertThat(mesh.getFunction2D(), is(func));
    }

    @Test
    @DisplayName("Mesh is updated when rangeX changes")
    public void testMeshUpdatesOnRangeXChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setRangeX(20.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when rangeY changes")
    public void testMeshUpdatesOnRangeYChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setRangeY(20.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when divisionsX changes")
    public void testMeshUpdatesOnDivisionsXChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setDivisionsX(32);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when divisionsY changes")
    public void testMeshUpdatesOnDivisionsYChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setDivisionsY(32);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when functionScale changes")
    public void testMeshUpdatesOnFunctionScaleChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setFunctionScale(2.0);

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
        assertThat(mesh.function2DProperty(), is(notNullValue()));
        assertThat(mesh.rangeXProperty(), is(notNullValue()));
        assertThat(mesh.rangeYProperty(), is(notNullValue()));
        assertThat(mesh.divisionsXProperty(), is(notNullValue()));
        assertThat(mesh.divisionsYProperty(), is(notNullValue()));
        assertThat(mesh.functionScaleProperty(), is(notNullValue()));
    }

    @Test
    @DisplayName("Polygon mesh is available")
    public void testPolygonMeshAvailable() {
        assertThat(mesh.getPolygonMesh(), is(notNullValue()));
    }
}
