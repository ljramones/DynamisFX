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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ScatterPlotMeshTest {

    private ScatterPlotMesh plot;

    @BeforeEach
    public void setUp() {
        plot = new ScatterPlotMesh(true);
    }

    @Test
    @DisplayName("Constructor with selfLit creates plot")
    public void testSelfLitConstruction() {
        assertThat(plot.selfLightEnabled, is(true));
        assertThat(plot.scatterDataGroup, is(notNullValue()));
    }

    @Test
    @DisplayName("Constructor with parameters sets values")
    public void testParameterizedConstruction() {
        plot = new ScatterPlotMesh(500, 2.0, false);

        assertThat(plot.nodeRadius, is(2.0));
    }

    @Test
    @DisplayName("setXYZData creates mesh views")
    public void testSetXYZData() {
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        ArrayList<Double> z = new ArrayList<>();
        x.add(1.0);
        x.add(2.0);
        y.add(3.0);
        y.add(4.0);
        z.add(5.0);
        z.add(6.0);

        plot.setXYZData(x, y, z);

        assertThat(plot.getChildren().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Default node type is sphere")
    public void testDefaultNodeType() {
        assertThat(plot.defaultNodeType, is(ScatterPlotMesh.NodeType.SPHERE));
    }
}
