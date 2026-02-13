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

package org.dynamisfx.shapes.polygon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PolygonMeshTest {

    private PolygonMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new PolygonMesh();
    }

    @Test
    @DisplayName("Default constructor creates empty mesh")
    public void testDefaultConstruction() {
        assertThat(mesh.getPoints(), is(notNullValue()));
        assertThat(mesh.getTexCoords(), is(notNullValue()));
        assertThat(mesh.getFaces(), is(notNullValue()));
        assertThat(mesh.getPoints().size(), is(0));
    }

    @Test
    @DisplayName("Constructor with arrays sets data")
    public void testArrayConstruction() {
        float[] points = {0, 0, 0, 1, 0, 0, 0, 1, 0};
        float[] texCoords = {0, 0, 1, 0, 0, 1};
        int[][] faces = {{0, 0, 1, 1, 2, 2}};

        mesh = new PolygonMesh(points, texCoords, faces);

        assertThat(mesh.getPoints().size(), is(9));
        assertThat(mesh.getTexCoords().size(), is(6));
        assertThat(mesh.getFaces().length, is(1));
    }

    @Test
    @DisplayName("getPointElementSize returns 3")
    public void testPointElementSize() {
        assertThat(mesh.getPointElementSize(), is(3));
    }

    @Test
    @DisplayName("getTexCoordElementSize returns 2")
    public void testTexCoordElementSize() {
        assertThat(mesh.getTexCoordElementSize(), is(2));
    }

    @Test
    @DisplayName("getFaceElementSize returns 6")
    public void testFaceElementSize() {
        assertThat(mesh.getFaceElementSize(), is(6));
    }

    @Test
    @DisplayName("getFaceSmoothingGroups returns observable array")
    public void testFaceSmoothingGroups() {
        assertThat(mesh.getFaceSmoothingGroups(), is(notNullValue()));
    }

    @Test
    @DisplayName("setFaces updates faces")
    public void testSetFaces() {
        int[][] newFaces = {{0, 0, 1, 1, 2, 2}, {1, 1, 2, 2, 3, 3}};
        mesh.setFaces(newFaces);

        assertThat(mesh.getFaces().length, is(2));
    }

    @Test
    @DisplayName("getNumEdgesInFaces calculates correctly")
    public void testNumEdgesInFaces() {
        int[][] faces = {{0, 0, 1, 1, 2, 2}, {0, 0, 2, 2, 3, 3}};
        mesh.setFaces(faces);

        // Each face has 3 edges (triangle), total 6 edges, but shared so 6/2 = 3?
        // Actually each face[].length = 6, which is 3 point/tex pairs
        // So numEdges = (6 + 6) / 2 = 6
        assertThat(mesh.getNumEdgesInFaces(), is(6));
    }
}
