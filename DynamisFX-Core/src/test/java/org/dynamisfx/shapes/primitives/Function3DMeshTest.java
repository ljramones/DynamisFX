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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Function3DMeshTest {

    private Function3DMesh mesh;
    private SurfacePlotMesh surfacePlotMesh;

    @BeforeEach
    public void setUp() {
        Function<Point2D, Number> func = p -> Math.sin(p.getX()) * Math.cos(p.getY());
        surfacePlotMesh = new SurfacePlotMesh(func, 10, 10, 16, 16, 1.0);
        mesh = new Function3DMesh(surfacePlotMesh, false);
    }

    @Test
    @DisplayName("Constructor sets surface correctly")
    public void testConstruction() {
        assertThat(mesh.getSurface(), is(surfacePlotMesh));
    }

    @Test
    @DisplayName("Constructor sets wireframe correctly")
    public void testWireframeConstruction() {
        assertThat(mesh.isWireframe(), is(false));

        mesh = new Function3DMesh(surfacePlotMesh, true);
        assertThat(mesh.isWireframe(), is(true));
    }

    @Test
    @DisplayName("Surface property accessor works")
    public void testSurfaceProperty() {
        assertThat(mesh.surfaceProperty(), is(notNullValue()));
    }

    @Test
    @DisplayName("Wireframe property accessor works")
    public void testWireframeProperty() {
        assertThat(mesh.wireframeProperty(), is(notNullValue()));
    }

    @Test
    @DisplayName("Function3D data is available")
    public void testFunction3DData() {
        assertThat(mesh.getFunction3DData(), is(notNullValue()));
        assertThat(mesh.getFunction3DData().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Group contains children")
    public void testGroupContainsChildren() {
        assertThat(mesh.getChildren().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Wireframe can be toggled")
    public void testWireframeToggle() {
        int childrenWithoutWireframe = mesh.getChildren().size();

        mesh.setWireframe(true);
        int childrenWithWireframe = mesh.getChildren().size();

        assertThat(childrenWithWireframe, greaterThan(childrenWithoutWireframe));
    }

    @Test
    @DisplayName("Surface can be changed")
    public void testSurfaceChange() {
        Function<Point2D, Number> newFunc = p -> p.getX() * p.getY();
        SurfacePlotMesh newSurface = new SurfacePlotMesh(newFunc, 5, 5, 8, 8, 1.0);

        mesh.setSurface(newSurface);

        assertThat(mesh.getSurface(), is(newSurface));
    }
}
