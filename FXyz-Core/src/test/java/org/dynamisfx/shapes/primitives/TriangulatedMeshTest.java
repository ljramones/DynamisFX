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

public class TriangulatedMeshTest {

    private TriangulatedMesh mesh;
    private List<Point3D> points;

    @BeforeEach
    public void setUp() {
        // Create a simple triangle
        points = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0),
            new Point3D(5, 10, 0)
        );
        mesh = new TriangulatedMesh(points);
    }

    @Test
    @DisplayName("Constructor with points uses default values")
    public void testPointsConstruction() {
        assertThat(mesh.getHeight(), is(1.0));
        assertThat(mesh.getLevel(), is(1));
        assertThat(mesh.getHoleRadius(), is(0.0));
    }

    @Test
    @DisplayName("Constructor with height sets value correctly")
    public void testHeightConstruction() {
        mesh = new TriangulatedMesh(points, 5.0);

        assertThat(mesh.getHeight(), is(5.0));
    }

    @Test
    @DisplayName("Mesh is updated when height changes")
    public void testMeshUpdatesOnHeightChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setHeight(10.0);

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
        assertThat(mesh.heightProperty(), is(notNullValue()));
        assertThat(mesh.levelProperty(), is(notNullValue()));
        assertThat(mesh.holeRadiusProperty(), is(notNullValue()));
        assertThat(mesh.boundsProperty(), is(notNullValue()));
    }

    @Test
    @DisplayName("Square polygon creates valid mesh")
    public void testSquarePolygon() {
        List<Point3D> square = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0),
            new Point3D(10, 10, 0),
            new Point3D(0, 10, 0)
        );
        mesh = new TriangulatedMesh(square);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
        assertThat(tm.getFaces().size(), greaterThan(0));
    }
}
