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
