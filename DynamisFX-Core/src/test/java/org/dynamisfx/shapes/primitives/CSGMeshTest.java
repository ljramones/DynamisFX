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

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.Sphere;
import javafx.scene.shape.TriangleMesh;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CSGMeshTest {

    @Test
    @DisplayName("CSGMesh from cube has vertices")
    public void testCubeHasVertices() {
        CSG cube = new Cube(10).toCSG();
        CSGMesh mesh = new CSGMesh(cube);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
    }

    @Test
    @DisplayName("CSGMesh from cube has faces")
    public void testCubeHasFaces() {
        CSG cube = new Cube(10).toCSG();
        CSGMesh mesh = new CSGMesh(cube);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getFaces().size(), greaterThan(0));
    }

    @Test
    @DisplayName("CSGMesh from sphere has vertices")
    public void testSphereHasVertices() {
        CSG sphere = new Sphere(5).toCSG();
        CSGMesh mesh = new CSGMesh(sphere);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
    }

    @Test
    @DisplayName("CSGMesh from union operation works")
    public void testUnionOperation() {
        CSG cube = new Cube(10).toCSG();
        CSG sphere = new Sphere(5).toCSG().transformed(eu.mihosoft.vvecmath.Transform.unity().translate(5, 0, 0));
        CSG union = cube.union(sphere);
        CSGMesh mesh = new CSGMesh(union);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
        assertThat(tm.getFaces().size(), greaterThan(0));
    }

    @Test
    @DisplayName("CSGMesh from difference operation works")
    public void testDifferenceOperation() {
        CSG cube = new Cube(10).toCSG();
        CSG sphere = new Sphere(5).toCSG();
        CSG difference = cube.difference(sphere);
        CSGMesh mesh = new CSGMesh(difference);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
        assertThat(tm.getFaces().size(), greaterThan(0));
    }

    @Test
    @DisplayName("CSGMesh from intersect operation works")
    public void testIntersectOperation() {
        CSG cube = new Cube(10).toCSG();
        CSG sphere = new Sphere(7).toCSG();
        CSG intersect = cube.intersect(sphere);
        CSGMesh mesh = new CSGMesh(intersect);

        TriangleMesh tm = (TriangleMesh) mesh.getMesh();
        assertThat(tm.getPoints().size(), greaterThan(0));
        assertThat(tm.getFaces().size(), greaterThan(0));
    }
}
