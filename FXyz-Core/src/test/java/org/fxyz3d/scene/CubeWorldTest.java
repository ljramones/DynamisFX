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

package org.fxyz3d.scene;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CubeWorldTest {

    private CubeWorld cubeWorld;

    @BeforeEach
    public void setUp() {
        cubeWorld = new CubeWorld(true);
    }

    @Test
    @DisplayName("Constructor with ambient light creates cube world")
    public void testAmbientLightConstruction() {
        assertThat(cubeWorld.isSelfLightEnabled(), is(true));
        assertThat(cubeWorld.getContentGroup(), is(notNullValue()));
    }

    @Test
    @DisplayName("Constructor with parameters sets values")
    public void testParameterizedConstruction() {
        cubeWorld = new CubeWorld(500, 50, false);

        assertThat(cubeWorld.isSelfLightEnabled(), is(false));
    }

    @Test
    @DisplayName("Axis materials are accessible")
    public void testAxisMaterials() {
        assertThat(cubeWorld.getXAxisMaterial(), is(notNullValue()));
        assertThat(cubeWorld.getYAxisMaterial(), is(notNullValue()));
        assertThat(cubeWorld.getZAxisMaterial(), is(notNullValue()));
    }

    @Test
    @DisplayName("Content group is accessible")
    public void testContentGroup() {
        assertThat(cubeWorld.getContentGroup(), is(notNullValue()));
        assertThat(cubeWorld.getContentGroup().getChildren(), is(notNullValue()));
    }

    @Test
    @DisplayName("Group contains children after init")
    public void testGroupContainsChildren() {
        assertThat(cubeWorld.getChildren().size(), greaterThan(0));
    }

    @Test
    @DisplayName("Self light enabled can be changed")
    public void testSelfLightEnabledChange() {
        cubeWorld.setSelfLightEnabled(false);

        assertThat(cubeWorld.isSelfLightEnabled(), is(false));
    }

    @Test
    @DisplayName("showAll affects visibility")
    public void testShowAll() {
        cubeWorld.showAll(false);
        cubeWorld.showAll(true);
        // No exception means success - visibility state is internal
    }

    @Test
    @DisplayName("Panel visibility methods work")
    public void testPanelVisibility() {
        cubeWorld.showX1Panel(false);
        cubeWorld.showX2Panel(false);
        cubeWorld.showY1Panel(false);
        cubeWorld.showY2Panel(false);
        cubeWorld.showZ1Panel(false);
        cubeWorld.showZ2Panel(false);
        // No exception means success
    }

    @Test
    @DisplayName("Axes visibility methods work")
    public void testAxesVisibility() {
        cubeWorld.showXAxesGroup(false);
        cubeWorld.showYAxesGroup(false);
        cubeWorld.showZAxesGroup(false);
        // No exception means success
    }

    @Test
    @DisplayName("Grid line visibility methods work")
    public void testGridLineVisibility() {
        cubeWorld.showXY1GridLinesGroup(false);
        cubeWorld.showXX1GridLinesGroup(false);
        cubeWorld.showYY1GridLinesGroup(false);
        cubeWorld.showYX1GridLinesGroup(false);
        cubeWorld.showZY1GridLinesGroup(false);
        cubeWorld.showZX1GridLinesGroup(false);
        cubeWorld.showXY2GridLinesGroup(false);
        cubeWorld.showXX2GridLinesGroup(false);
        cubeWorld.showYY2GridLinesGroup(false);
        cubeWorld.showYX2GridLinesGroup(false);
        cubeWorld.showZY2GridLinesGroup(false);
        cubeWorld.showZX2GridLinesGroup(false);
        // No exception means success
    }

    @Test
    @DisplayName("showAllGridLines convenience method works")
    public void testShowAllGridLines() {
        cubeWorld.showAllGridLines(false);
        cubeWorld.showAllGridLines(true);
        // No exception means success
    }

    @Test
    @DisplayName("adjustPanelsByPos handles various angles")
    public void testAdjustPanelsByPos() {
        cubeWorld.adjustPanelsByPos(0, 0, 0);
        cubeWorld.adjustPanelsByPos(45, 90, 0);
        cubeWorld.adjustPanelsByPos(-45, -90, 0);
        cubeWorld.adjustPanelsByPos(0, 180, 0);
        cubeWorld.adjustPanelsByPos(0, -180, 0);
        // No exception means success
    }
}
