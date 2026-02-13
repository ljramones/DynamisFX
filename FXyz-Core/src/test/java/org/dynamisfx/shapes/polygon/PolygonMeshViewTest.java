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

import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PolygonMeshViewTest {

    private PolygonMeshView meshView;

    @BeforeEach
    public void setUp() {
        meshView = new PolygonMeshView();
    }

    @Test
    @DisplayName("Default constructor creates view")
    public void testDefaultConstruction() {
        assertThat(meshView, is(notNullValue()));
    }

    @Test
    @DisplayName("Constructor with mesh sets mesh")
    public void testMeshConstruction() {
        PolygonMesh mesh = new PolygonMesh();
        meshView = new PolygonMeshView(mesh);

        assertThat(meshView.getMesh(), is(mesh));
    }

    @Test
    @DisplayName("Default draw mode is FILL")
    public void testDefaultDrawMode() {
        assertThat(meshView.getDrawMode(), is(DrawMode.FILL));
    }

    @Test
    @DisplayName("Draw mode can be changed")
    public void testSetDrawMode() {
        meshView.setDrawMode(DrawMode.LINE);

        assertThat(meshView.getDrawMode(), is(DrawMode.LINE));
    }

    @Test
    @DisplayName("Default cull face is BACK")
    public void testDefaultCullFace() {
        assertThat(meshView.getCullFace(), is(CullFace.BACK));
    }

    @Test
    @DisplayName("Cull face can be changed")
    public void testSetCullFace() {
        meshView.setCullFace(CullFace.NONE);

        assertThat(meshView.getCullFace(), is(CullFace.NONE));
    }

    @Test
    @DisplayName("Subdivision level defaults to 0")
    public void testDefaultSubdivisionLevel() {
        assertThat(meshView.getSubdivisionLevel(), is(0));
    }

    @Test
    @DisplayName("Property accessors work")
    public void testPropertyAccessors() {
        assertThat(meshView.meshProperty(), is(notNullValue()));
        assertThat(meshView.drawModeProperty(), is(notNullValue()));
        assertThat(meshView.cullFaceProperty(), is(notNullValue()));
        assertThat(meshView.materialProperty(), is(notNullValue()));
        assertThat(meshView.subdivisionLevelProperty(), is(notNullValue()));
    }
}
