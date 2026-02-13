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
