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

package org.fxyz3d.shapes.primitives;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SVG3DMeshTest {

    private SVG3DMesh mesh;

    @BeforeEach
    public void setUp() {
        mesh = new SVG3DMesh();
    }

    @Test
    @DisplayName("Default constructor uses correct default values")
    public void testDefaultConstruction() {
        assertThat(mesh.getContent(), is("M40,60 C42,48 44,30 25,32"));
        assertThat(mesh.getHeight(), is(50.0));
        assertThat(mesh.getLevel(), is(1));
        assertThat(mesh.isJoinSegments(), is(true));
    }

    @Test
    @DisplayName("Content constructor sets content correctly")
    public void testContentConstruction() {
        String svgPath = "M10,10 L50,10 L50,50 L10,50 Z";
        mesh = new SVG3DMesh(svgPath);

        assertThat(mesh.getContent(), is(svgPath));
    }

    @Test
    @DisplayName("Content and height constructor sets values correctly")
    public void testContentHeightConstruction() {
        String svgPath = "M10,10 L50,10 L50,50 L10,50 Z";
        mesh = new SVG3DMesh(svgPath, 100.0);

        assertThat(mesh.getContent(), is(svgPath));
        assertThat(mesh.getHeight(), is(100.0));
    }

    @Test
    @DisplayName("Full constructor sets all values correctly")
    public void testFullConstruction() {
        String svgPath = "M10,10 L50,10 L50,50 L10,50 Z";
        mesh = new SVG3DMesh(svgPath, 75.0, 2, false);

        assertThat(mesh.getContent(), is(svgPath));
        assertThat(mesh.getHeight(), is(75.0));
        assertThat(mesh.getLevel(), is(2));
        assertThat(mesh.isJoinSegments(), is(false));
    }

    @Test
    @DisplayName("Group contains children")
    public void testGroupContainsChildren() {
        assertThat(mesh.getChildren().size(), greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Property accessors work correctly")
    public void testPropertyAccessors() {
        assertThat(mesh.contentProperty(), is(notNullValue()));
        assertThat(mesh.heightProperty(), is(notNullValue()));
        assertThat(mesh.levelProperty(), is(notNullValue()));
        assertThat(mesh.joinSegmentsProperty(), is(notNullValue()));
    }

    @Test
    @DisplayName("Content can be changed")
    public void testContentChange() {
        String newPath = "M0,0 L100,0 L100,100 L0,100 Z";
        mesh.setContent(newPath);

        assertThat(mesh.getContent(), is(newPath));
    }

    @Test
    @DisplayName("Height can be changed")
    public void testHeightChange() {
        mesh.setHeight(200.0);

        assertThat(mesh.getHeight(), is(200.0));
    }

    @Test
    @DisplayName("Level can be changed")
    public void testLevelChange() {
        mesh.setLevel(3);

        assertThat(mesh.getLevel(), is(3));
    }

    @Test
    @DisplayName("JoinSegments can be changed")
    public void testJoinSegmentsChange() {
        mesh.setJoinSegments(false);

        assertThat(mesh.isJoinSegments(), is(false));
    }
}
