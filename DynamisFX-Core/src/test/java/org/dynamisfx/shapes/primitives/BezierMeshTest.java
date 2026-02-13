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
