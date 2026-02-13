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

public class SpheroidMeshTest {

    private SpheroidMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new SpheroidMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getMajorRadius(), is(50.0));
        assertThat(mesh.getMinorRadius(), is(12.0));
        assertThat(mesh.getDivisions(), is(64));
    }

    @Test
    @DisplayName("Single radius constructor creates sphere")
    public void testSingleRadiusConstruction() {
        mesh = new SpheroidMesh(25.0);

        assertThat(mesh.getMajorRadius(), is(25.0));
        assertThat(mesh.getMinorRadius(), is(25.0));
        assertThat(mesh.isSphere(), is(true));
    }

    @Test
    @DisplayName("Two-parameter constructor sets values correctly")
    public void testTwoParamConstruction() {
        mesh = new SpheroidMesh(30.0, 15.0);

        assertThat(mesh.getMajorRadius(), is(30.0));
        assertThat(mesh.getMinorRadius(), is(15.0));
    }

    @Test
    @DisplayName("Three-parameter constructor sets values correctly")
    public void testThreeParamConstruction() {
        mesh = new SpheroidMesh(32, 40.0, 20.0);

        assertThat(mesh.getDivisions(), is(32));
        assertThat(mesh.getMajorRadius(), is(40.0));
        assertThat(mesh.getMinorRadius(), is(20.0));
    }

    @Test
    @DisplayName("Mesh is updated when major radius changes")
    public void testMeshUpdatesOnMajorRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setMajorRadius(100.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when minor radius changes")
    public void testMeshUpdatesOnMinorRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setMinorRadius(30.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when divisions changes")
    public void testMeshUpdatesOnDivisionsChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setDivisions(32);

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
    @DisplayName("isSphere returns true when radii are equal")
    public void testIsSphere() {
        mesh = new SpheroidMesh(30.0);

        assertThat(mesh.isSphere(), is(true));
        assertThat(mesh.isOblateSpheroid(), is(false));
        assertThat(mesh.isProlateSpheroid(), is(false));
    }

    @Test
    @DisplayName("isOblateSpheroid returns true when major > minor")
    public void testIsOblateSpheroid() {
        mesh = new SpheroidMesh(50.0, 30.0);

        assertThat(mesh.isOblateSpheroid(), is(true));
        assertThat(mesh.isSphere(), is(false));
        assertThat(mesh.isProlateSpheroid(), is(false));
    }

    @Test
    @DisplayName("isProlateSpheroid returns true when major < minor")
    public void testIsProlateSpheroid() {
        mesh = new SpheroidMesh(30.0, 50.0);

        assertThat(mesh.isProlateSpheroid(), is(true));
        assertThat(mesh.isSphere(), is(false));
        assertThat(mesh.isOblateSpheroid(), is(false));
    }

    @Test
    @DisplayName("Property accessors work correctly")
    public void testPropertyAccessors() {
        assertThat(mesh.majorRadiusProperty(), is(notNullValue()));
        assertThat(mesh.minorRadiusProperty(), is(notNullValue()));
        assertThat(mesh.divisionsProperty(), is(notNullValue()));
    }
}
