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

import javafx.scene.shape.Mesh;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class CapsuleMeshViewTest {

    private CapsuleMesh meshView;

    @BeforeEach
    public void setUp() {
        meshView = new CapsuleMesh();
    }

    @Test
    public void testConstruction() {
        meshView = new CapsuleMesh(80, 15.0, 25.0);

        assertThat(meshView.getDivisions(), is(80));
        assertThat(meshView.getRadius(), is(15.0));
        assertThat(meshView.getHeight(), is(25.0));
    }

    @Test
    @DisplayName("Mesh is updated when num divisions changes")
    public void testUpdateMeshDivisions() {
        Mesh oldMesh = meshView.getMesh();

        meshView.setDivisions(80);

        assertThat(meshView.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when radius changes")
    public void testUpdateMeshRadius() {
        Mesh oldMesh = meshView.getMesh();

        meshView.setRadius(80);

        assertThat(meshView.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when height changes")
    public void testUpdateHeight() {
        Mesh oldMesh = meshView.getMesh();

        meshView.setHeight(80);

        assertThat(meshView.getMesh(), is(not(oldMesh)));
    }
}
