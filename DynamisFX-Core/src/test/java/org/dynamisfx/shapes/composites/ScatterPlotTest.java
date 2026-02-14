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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ScatterPlotTest {

    private ScatterPlot plot;

    @BeforeEach
    public void setUp() {
        plot = new ScatterPlot(true);
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
        plot = new ScatterPlot(500, 2.0, false);

        assertThat(plot.nodeRadius, is(2.0));
        assertThat(plot.selfLightEnabled, is(false));
    }

    @Test
    @DisplayName("setXYZData creates data nodes")
    public void testSetXYZData() {
        List<Double> x = Arrays.asList(1.0, 2.0, 3.0);
        List<Double> y = Arrays.asList(4.0, 5.0, 6.0);
        List<Double> z = Arrays.asList(7.0, 8.0, 9.0);

        plot.setXYZData(x, y, z);

        assertThat(plot.scatterDataGroup.getChildren().size(), is(3));
    }

    @Test
    @DisplayName("Axis data accessors work")
    public void testAxisDataAccessors() {
        List<Double> x = Arrays.asList(1.0, 2.0);

        plot.setxAxisData(x);

        assertThat(plot.getxAxisData(), is(x));
    }

    @Test
    @DisplayName("Node type can be changed")
    public void testNodeType() {
        plot.setDefaultNodeType(ScatterPlot.NodeType.CUBE);

        assertThat(plot.getDefaultNodeType(), is(ScatterPlot.NodeType.CUBE));
    }

    @Test
    @DisplayName("Group contains children after init")
    public void testGroupContainsChildren() {
        assertThat(plot.getChildren().size(), greaterThan(0));
    }
}
