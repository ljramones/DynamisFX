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

public class HistogramTest {

    private Histogram histogram;

    @BeforeEach
    public void setUp() {
        histogram = new Histogram(true);
    }

    @Test
    @DisplayName("Constructor with selfLit creates histogram")
    public void testSelfLitConstruction() {
        assertThat(histogram.selfLightEnabled, is(true));
        assertThat(histogram.histogramDataGroup, is(notNullValue()));
    }

    @Test
    @DisplayName("Constructor with parameters sets values")
    public void testParameterizedConstruction() {
        histogram = new Histogram(500, 2.0, false);

        assertThat(histogram.nodeRadius, is(2.0));
        assertThat(histogram.selfLightEnabled, is(false));
    }

    @Test
    @DisplayName("setHeightData creates bars")
    public void testSetHeightData() {
        float[][] data = {
            {1.0f, 2.0f},
            {3.0f, 4.0f}
        };

        histogram.setHeightData(data, 10, 20, Color.BLUE, true, true);

        assertThat(histogram.histogramDataGroup.getChildren().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Node type can be changed")
    public void testNodeType() {
        histogram.setDefaultNodeType(Histogram.NodeType.CYLINDER);

        assertThat(histogram.getDefaultNodeType(), is(Histogram.NodeType.CYLINDER));
    }

    @Test
    @DisplayName("Group contains children after init")
    public void testGroupContainsChildren() {
        assertThat(histogram.getChildren().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Histogram with cube node type")
    public void testCubeNodeType() {
        histogram.setDefaultNodeType(Histogram.NodeType.CUBE);
        float[][] data = {{5.0f}};

        histogram.setHeightData(data, 10, 20, Color.RED, false, true);

        assertThat(histogram.histogramDataGroup.getChildren().size(), greaterThan(0));
    }
}
