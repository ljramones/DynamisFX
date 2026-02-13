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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Text3DMeshTest {

    private Text3DMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new Text3DMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getText3D(), is("F(X)yz 3D"));
        assertThat(mesh.getFont(), is("Arial"));
        assertThat(mesh.getFontSize(), is(100));
        assertThat(mesh.getHeight(), is(10.0));
        assertThat(mesh.getGap(), is(0.0));
        assertThat(mesh.getLevel(), is(1));
        assertThat(mesh.isJoinSegments(), is(true));
    }

    @Test
    @DisplayName("Text constructor sets text correctly")
    public void testTextConstruction() {
        mesh = new Text3DMesh("Hello");

        assertThat(mesh.getText3D(), is("Hello"));
    }

    @Test
    @DisplayName("Text and font constructor sets values correctly")
    public void testTextFontConstruction() {
        mesh = new Text3DMesh("Test", "Helvetica", 50);

        assertThat(mesh.getText3D(), is("Test"));
        assertThat(mesh.getFont(), is("Helvetica"));
        assertThat(mesh.getFontSize(), is(50));
    }

    @Test
    @DisplayName("Text and height constructor sets values correctly")
    public void testTextHeightConstruction() {
        mesh = new Text3DMesh("ABC", 20.0);

        assertThat(mesh.getText3D(), is("ABC"));
        assertThat(mesh.getHeight(), is(20.0));
    }

    @Test
    @DisplayName("Group contains meshes for each letter")
    public void testGroupContainsMeshes() {
        mesh = new Text3DMesh("AB");

        assertThat(mesh.getChildren().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Property accessors work correctly")
    public void testPropertyAccessors() {
        assertThat(mesh.text3DProperty(), is(notNullValue()));
        assertThat(mesh.fontProperty(), is(notNullValue()));
        assertThat(mesh.fontSizeProperty(), is(notNullValue()));
        assertThat(mesh.heightProperty(), is(notNullValue()));
        assertThat(mesh.gapProperty(), is(notNullValue()));
        assertThat(mesh.levelProperty(), is(notNullValue()));
        assertThat(mesh.joinSegmentsProperty(), is(notNullValue()));
    }

    @Test
    @DisplayName("getMeshes returns list of meshes")
    public void testGetMeshes() {
        assertThat(mesh.getMeshes(), is(notNullValue()));
        assertThat(mesh.getMeshes().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Text can be changed")
    public void testTextChange() {
        mesh.setText3D("XYZ");

        assertThat(mesh.getText3D(), is("XYZ"));
    }

    @Test
    @DisplayName("Height can be changed")
    public void testHeightChange() {
        mesh.setHeight(25.0);

        assertThat(mesh.getHeight(), is(25.0));
    }

    @Test
    @DisplayName("Count bindings work")
    public void testCountBindings() {
        assertThat(mesh.vertCountBinding(), is(notNullValue()));
        assertThat(mesh.faceCountBinding(), is(notNullValue()));
    }
}
