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
        assertThat(mesh.getText3D(), is("DynamisFX 3D"));
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
