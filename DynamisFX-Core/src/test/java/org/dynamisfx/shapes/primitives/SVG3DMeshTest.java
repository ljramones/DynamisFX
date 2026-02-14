/*
 * Copyright 2024-2026 DynamisFX Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file contains code derived from FXyz (https://github.com/FXyz/FXyz),
 * originally licensed under the BSD 3-Clause License.
 * Copyright (c) 2013-2019, F(X)yz. All rights reserved.
 */

package org.dynamisfx.shapes.primitives;

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
