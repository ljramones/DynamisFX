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

package org.dynamisfx.shapes.composites;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SurfacePlotTest {

    private SurfacePlot plot;

    @BeforeEach
    public void setUp() {
        plot = new SurfacePlot(true);
    }

    @Test
    @DisplayName("Constructor with selfLit creates plot")
    public void testSelfLitConstruction() {
        assertThat(plot.selfLightEnabled, is(true));
    }

    @Test
    @DisplayName("Constructor with data creates mesh")
    public void testDataConstruction() {
        float[][] data = {
            {0.0f, 1.0f, 2.0f},
            {1.0f, 2.0f, 3.0f},
            {2.0f, 3.0f, 4.0f}
        };

        plot = new SurfacePlot(data, 10, Color.BLUE, true, true);

        assertThat(plot.meshView, is(notNullValue()));
    }

    @Test
    @DisplayName("setHeightData creates mesh view")
    public void testSetHeightData() {
        float[][] data = {
            {0.0f, 1.0f},
            {1.0f, 2.0f}
        };

        plot.setHeightData(data, 10, Color.RED, true, true);

        assertThat(plot.meshView, is(notNullValue()));
        assertThat(plot.material, is(notNullValue()));
    }

    @Test
    @DisplayName("MeshView is added to children")
    public void testMeshViewInChildren() {
        float[][] data = {
            {0.0f, 1.0f},
            {1.0f, 2.0f}
        };

        plot.setHeightData(data, 10, Color.GREEN, false, false);

        assertThat(plot.getChildren(), hasItem(plot.meshView));
    }

    @Test
    @DisplayName("Wire mode creates line mesh")
    public void testWireMode() {
        float[][] data = {
            {0.0f, 1.0f},
            {1.0f, 2.0f}
        };

        plot.setHeightData(data, 10, Color.WHITE, false, false);

        assertThat(plot.meshView, is(notNullValue()));
    }
}
