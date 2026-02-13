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

public class SegmentedDomeMeshTest {

    private SegmentedDomeMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new SegmentedDomeMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getRadius(), is(50.0));
        assertThat(mesh.getPhimin(), is(0.0));
        assertThat(mesh.getPhimax(), is(Math.toRadians(360)));
        assertThat(mesh.getThetamin(), is(0.0));
        assertThat(mesh.getThetamax(), is(Math.toRadians(90)));
        assertThat(mesh.getDivisions(), is(20));
    }

    @Test
    @DisplayName("Mesh is updated when radius changes")
    public void testMeshUpdatesOnRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setRadius(100.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when phimax changes")
    public void testMeshUpdatesOnPhimaxChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setPhimax(Math.toRadians(180));

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when thetamax changes")
    public void testMeshUpdatesOnThetamaxChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setThetamax(Math.toRadians(45));

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when divisions changes")
    public void testMeshUpdatesOnDivisionsChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setDivisions(40);

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
        assertThat(mesh.radiusProperty(), is(notNullValue()));
        assertThat(mesh.phiminProperty(), is(notNullValue()));
        assertThat(mesh.phimaxProperty(), is(notNullValue()));
        assertThat(mesh.thetaminProperty(), is(notNullValue()));
        assertThat(mesh.thetamaxProperty(), is(notNullValue()));
        assertThat(mesh.divisionsProperty(), is(notNullValue()));
    }
}
