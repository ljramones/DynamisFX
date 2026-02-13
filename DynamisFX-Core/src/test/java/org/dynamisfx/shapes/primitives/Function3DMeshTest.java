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
