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

public class SegmentedTorusMeshTest {

    private SegmentedTorusMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new SegmentedTorusMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getMajorRadiusDivisions(), is(64));
        assertThat(mesh.getMinorRadiusDivisions(), is(64));
        assertThat(mesh.getMajorRadiusCrop(), is(0));
        assertThat(mesh.getMajorRadius(), is(12.5));
        assertThat(mesh.getMinorRadius(), is(5.0));
    }

    @Test
    @DisplayName("Two-parameter constructor sets values correctly")
    public void testTwoParamConstruction() {
        mesh = new SegmentedTorusMesh(20.0, 8.0);

        assertThat(mesh.getMajorRadius(), is(20.0));
        assertThat(mesh.getMinorRadius(), is(8.0));
    }

    @Test
    @DisplayName("Mesh is updated when major radius changes")
    public void testMeshUpdatesOnMajorRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setMajorRadius(25.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when minor radius changes")
    public void testMeshUpdatesOnMinorRadiusChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setMinorRadius(10.0);

        assertThat(mesh.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when divisions changes")
    public void testMeshUpdatesOnDivisionsChange() {
        Mesh oldMesh = mesh.getMesh();

        mesh.setMajorRadiusDivisions(32);

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
        assertThat(mesh.radiusMajorProperty(), is(notNullValue()));
        assertThat(mesh.minorRadiusProperty(), is(notNullValue()));
        assertThat(mesh.majorRadiusDivisionsProperty(), is(notNullValue()));
        assertThat(mesh.minorRadiusDivisionsProperty(), is(notNullValue()));
        assertThat(mesh.majorRadiusCropProperty(), is(notNullValue()));
    }
}
