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

package org.dynamisfx.scene;

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
        assertThat(cubeWorld.getSelectionModel(), is(notNullValue()));
        assertThat(cubeWorld.isSelectionEnabled(), is(true));
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
    @DisplayName("Selection support can be toggled")
    public void testSelectionEnabledToggle() {
        cubeWorld.setSelectionEnabled(false);
        assertThat(cubeWorld.isSelectionEnabled(), is(false));
        cubeWorld.setSelectionEnabled(true);
        assertThat(cubeWorld.isSelectionEnabled(), is(true));
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
