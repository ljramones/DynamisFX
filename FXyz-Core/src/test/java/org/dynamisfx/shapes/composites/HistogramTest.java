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
