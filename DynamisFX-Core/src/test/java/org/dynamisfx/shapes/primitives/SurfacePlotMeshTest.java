/*
 * F(X)yz
 *
 * Copyright (c) 2013-2021, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
