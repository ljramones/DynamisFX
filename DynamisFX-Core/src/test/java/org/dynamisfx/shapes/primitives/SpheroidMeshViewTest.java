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
import static org.hamcrest.Matchers.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class SpheroidMeshViewTest {

    private SpheroidMesh meshView;

    @BeforeEach
    public void setUp() {
        meshView = new SpheroidMesh();
    }

    @Test
    public void testConstruction() {
        meshView = new SpheroidMesh(32, 40.0, 8.0);

        assertThat(meshView.getDivisions(), is(32));
        assertThat(meshView.getMajorRadius(), is(40.0));
        assertThat(meshView.getMinorRadius(), is(8.0));
    }

    @Test
    @DisplayName("Mesh is updated when major radius changes")
    public void testUpdateMeshMajorRadius() {
        Mesh oldMesh = meshView.getMesh();

        meshView.setMajorRadius(24.0);

        assertThat(meshView.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when minor radius changes")
    public void testUpdateMeshMinorRadius() {
        Mesh oldMesh = meshView.getMesh();

        meshView.setMinorRadius(6.0);

        assertThat(meshView.getMesh(), is(not(oldMesh)));
    }

    @Test
    @DisplayName("Mesh is updated when number of divisions changes")
    public void testUpdateMeshNumDivisions() {
        Mesh oldMesh = meshView.getMesh();

        meshView.setDivisions(24);

        assertThat(meshView.getMesh(), is(not(oldMesh)));
    }
}
