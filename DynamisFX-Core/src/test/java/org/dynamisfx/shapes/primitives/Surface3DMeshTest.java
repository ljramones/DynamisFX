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

import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;
import org.dynamisfx.geometry.Point3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Surface3DMeshTest {

    private Surface3DMesh mesh;
    private List<Point3D> surfaceData;

    @BeforeEach
    public void setUp() {
        // Create a simple set of 3D points for Delaunay triangulation
        surfaceData = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0),
            new Point3D(5, 10, 0),
            new Point3D(0, 10, 5),
            new Point3D(10, 10, 5)
        );
        mesh = new Surface3DMesh(surfaceData);
    }

    @Test
    @DisplayName("Default constructor creates mesh with empty data")
    public void testDefaultConstruction() {
        mesh = new Surface3DMesh();

        assertThat(mesh.getSurfaceData(), is(notNullValue()));
    }

    @Test
    @DisplayName("Constructor with data sets surface data correctly")
    public void testDataConstruction() {
        assertThat(mesh.getSurfaceData(), is(surfaceData));
    }

    @Test
    @DisplayName("Mesh is updated when surface data changes")
    public void testMeshUpdatesOnSurfaceDataChange() {
        Mesh oldMesh = mesh.getMesh();

        List<Point3D> newData = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(20, 0, 0),
            new Point3D(10, 20, 0),
            new Point3D(0, 20, 10)
        );
        mesh.setSurfaceData(newData);

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
    @DisplayName("Property accessor works correctly")
    public void testPropertyAccessor() {
        assertThat(mesh.surfaceDataProperty(), is(notNullValue()));
    }
}
