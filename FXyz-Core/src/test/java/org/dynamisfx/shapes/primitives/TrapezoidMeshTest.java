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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TrapezoidMeshTest {

    private TrapezoidMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new TrapezoidMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getSmallSize(), is(30.0));
        assertThat(mesh.getBigSize(), is(50.0));
        assertThat(mesh.getHeight(), is(40.0));
        assertThat(mesh.getDepth(), is(60.0));
    }

    @Test
    @DisplayName("Parameterized constructor sets values correctly")
    public void testParameterizedConstruction() {
        mesh = new TrapezoidMesh(20.0, 40.0, 30.0, 50.0);

        assertThat(mesh.getSmallSize(), is(20.0));
        assertThat(mesh.getBigSize(), is(40.0));
        assertThat(mesh.getHeight(), is(30.0));
        assertThat(mesh.getDepth(), is(50.0));
    }

    @Test
    @DisplayName("Mesh is updated when smallSize changes")
    public void testMeshUpdatesOnSmallSizeChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setSmallSize(15.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when bigSize changes")
    public void testMeshUpdatesOnBigSizeChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setBigSize(100.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when height changes")
    public void testMeshUpdatesOnHeightChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setheight(80.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when depth changes")
    public void testMeshUpdatesOnDepthChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setDepth(100.0);

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
    @DisplayName("Trapezoid has exactly 8 vertices")
    public void testTrapezoidVertexCount() {
        TriangleMesh tm = (TriangleMesh) mesh.getMesh();

        // 8 vertices * 3 coordinates (x, y, z)
        assertThat(tm.getPoints().size(), is(24));
    }

    @Test
    @DisplayName("Trapezoid has exactly 12 faces")
    public void testTrapezoidFaceCount() {
        TriangleMesh tm = (TriangleMesh) mesh.getMesh();

        // 12 faces * 6 indices (3 vertex indices + 3 texture indices)
        assertThat(tm.getFaces().size(), is(72));
    }

    @Test
    @DisplayName("Property accessors work correctly")
    public void testPropertyAccessors() {
        assertThat(mesh.sizeSmallProperty(), is(notNullValue()));
        assertThat(mesh.sizeBigProperty(), is(notNullValue()));
        assertThat(mesh.heightProperty(), is(notNullValue()));
        assertThat(mesh.depthProperty(), is(notNullValue()));
    }
}
