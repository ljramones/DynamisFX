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

package org.fxyz3d.shapes.composites;

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
