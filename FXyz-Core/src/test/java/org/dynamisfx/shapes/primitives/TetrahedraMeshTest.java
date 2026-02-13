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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TetrahedraMeshTest {

    private TetrahedraMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new TetrahedraMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getHeight(), is(10.0));
        assertThat(mesh.getLevel(), is(1));
    }

    @Test
    @DisplayName("Height constructor sets height correctly")
    public void testHeightConstruction() {
        mesh = new TetrahedraMesh(20.0);

        assertThat(mesh.getHeight(), is(20.0));
        assertThat(mesh.getLevel(), is(1));
    }

    @Test
    @DisplayName("Full constructor sets all values correctly")
    public void testFullConstruction() {
        Point3D center = new Point3D(5, 5, 5);
        mesh = new TetrahedraMesh(15.0, 2, center);

        assertThat(mesh.getHeight(), is(15.0));
        assertThat(mesh.getLevel(), is(2));
        assertThat(mesh.getCenter(), is(center));
    }

    @Test
    @DisplayName("Mesh is updated when height changes")
    public void testMeshUpdatesOnHeightChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setHeight(30.0);

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
    @DisplayName("Mesh is updated when center changes")
    public void testMeshUpdatesOnCenterChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setCenter(new Point3D(10, 10, 10));

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
        assertThat(mesh.heightProperty(), is(notNullValue()));
        assertThat(mesh.levelProperty(), is(notNullValue()));
        assertThat(mesh.centerProperty(), is(notNullValue()));
    }

    @Test
    @DisplayName("Higher level creates more faces")
    public void testLevelIncreasesComplexity() {
        mesh = new TetrahedraMesh(10.0, 1, null);
        TriangleMesh level1Mesh = (TriangleMesh) mesh.getMesh();
        int level1Faces = level1Mesh.getFaces().size();

        mesh = new TetrahedraMesh(10.0, 2, null);
        TriangleMesh level2Mesh = (TriangleMesh) mesh.getMesh();
        int level2Faces = level2Mesh.getFaces().size();

        assertThat(level2Faces, greaterThan(level1Faces));
    }
}
