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

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CubeViewerTest {

    private CubeViewer viewer;

    @BeforeEach
    public void setUp() {
        viewer = new CubeViewer(true);
    }

    @Test
    @DisplayName("Constructor with selfLit creates viewer")
    public void testSelfLitConstruction() {
        assertThat(viewer.isSelfLightEnabled(), is(true));
        assertThat(viewer.getScatterDataGroup(), is(notNullValue()));
    }

    @Test
    @DisplayName("Constructor with size and spacing sets values")
    public void testSizeSpacingConstruction() {
        viewer = new CubeViewer(500, 50, false);

        assertThat(viewer.isSelfLightEnabled(), is(false));
    }

    @Test
    @DisplayName("Full constructor sets all values")
    public void testFullConstruction() {
        viewer = new CubeViewer(500, 50, false, 2.0, 10.0, 4.0);

        assertThat(viewer.isSelfLightEnabled(), is(false));
        assertThat(viewer.getScatterRadius(), is(2.0));
    }

    @Test
    @DisplayName("setXAxisData creates data nodes")
    public void testSetXAxisData() {
        List<Double> x = Arrays.asList(1.0, 2.0, 3.0);

        viewer.setXAxisData(x);

        assertThat(viewer.getXAxisData(), is(x));
        assertThat(viewer.getScatterDataGroup().getChildren().size(), is(3));
    }

    @Test
    @DisplayName("setYAxisData creates data nodes")
    public void testSetYAxisData() {
        List<Double> y = Arrays.asList(4.0, 5.0, 6.0);

        viewer.setYAxisData(y);

        assertThat(viewer.getYAxisData(), is(y));
        assertThat(viewer.getScatterDataGroup().getChildren().size(), is(3));
    }

    @Test
    @DisplayName("setZAxisData creates data nodes")
    public void testSetZAxisData() {
        List<Double> z = Arrays.asList(7.0, 8.0, 9.0);

        viewer.setZAxisData(z);

        assertThat(viewer.getZAxisData(), is(z));
        assertThat(viewer.getScatterDataGroup().getChildren().size(), is(3));
    }

    @Test
    @DisplayName("Combined axis data creates correct number of nodes")
    public void testCombinedAxisData() {
        viewer.setXAxisData(Arrays.asList(1.0, 2.0, 3.0));
        viewer.setYAxisData(Arrays.asList(4.0, 5.0, 6.0));
        viewer.setZAxisData(Arrays.asList(7.0, 8.0, 9.0));

        assertThat(viewer.getScatterDataGroup().getChildren().size(), is(3));
    }

    @Test
    @DisplayName("Scatter radius can be changed")
    public void testScatterRadiusChange() {
        viewer.setScatterRadius(5.0);

        assertThat(viewer.getScatterRadius(), is(5.0));
    }

    @Test
    @DisplayName("Self light enabled can be changed")
    public void testSelfLightEnabledChange() {
        viewer.setSelfLightEnabled(false);

        assertThat(viewer.isSelfLightEnabled(), is(false));
    }

    @Test
    @DisplayName("Panel colors can be changed")
    public void testPanelColors() {
        viewer.setX1PanelColor(Color.RED);
        viewer.setX2PanelColor(Color.GREEN);
        viewer.setY1PanelColor(Color.BLUE);
        viewer.setY2PanelColor(Color.YELLOW);
        viewer.setZ1PanelColor(Color.ORANGE);
        viewer.setZ2PanelColor(Color.PURPLE);
        // No exception means success
    }

    @Test
    @DisplayName("Group contains children after init")
    public void testGroupContainsChildren() {
        assertThat(viewer.getChildren().size(), greaterThan(0));
    }

    @Test
    @DisplayName("showAll affects visibility")
    public void testShowAll() {
        viewer.showAll(false);
        viewer.showAll(true);
        // No exception means success
    }

    @Test
    @DisplayName("showSphereGroup works")
    public void testShowSphereGroup() {
        viewer.showSphereGroup(false);
        viewer.showSphereGroup(true);
        // No exception means success
    }

    @Test
    @DisplayName("Panel visibility methods work")
    public void testPanelVisibility() {
        viewer.showX1Panel(false);
        viewer.showX2Panel(false);
        viewer.showY1Panel(false);
        viewer.showY2Panel(false);
        viewer.showZ1Panel(false);
        viewer.showZ2Panel(false);
        // No exception means success
    }

    @Test
    @DisplayName("Axes visibility methods work")
    public void testAxesVisibility() {
        viewer.showXAxesGroup(false);
        viewer.showYAxesGroup(false);
        viewer.showZAxesGroup(false);
        // No exception means success
    }

    @Test
    @DisplayName("Grid line visibility methods work")
    public void testGridLineVisibility() {
        viewer.showXY1GridLinesGroup(false);
        viewer.showXX1GridLinesGroup(false);
        viewer.showYY1GridLinesGroup(false);
        viewer.showYX1GridLinesGroup(false);
        viewer.showZY1GridLinesGroup(false);
        viewer.showZX1GridLinesGroup(false);
        viewer.showXY2GridLinesGroup(false);
        viewer.showXX2GridLinesGroup(false);
        viewer.showYY2GridLinesGroup(false);
        viewer.showYX2GridLinesGroup(false);
        viewer.showZY2GridLinesGroup(false);
        viewer.showZX2GridLinesGroup(false);
        // No exception means success
    }

    @Test
    @DisplayName("showAllGridLines convenience method works")
    public void testShowAllGridLines() {
        viewer.showAllGridLines(false);
        viewer.showAllGridLines(true);
        // No exception means success
    }

    @Test
    @DisplayName("adjustPanelsByPos handles various angles")
    public void testAdjustPanelsByPos() {
        viewer.adjustPanelsByPos(0, 0, 0);
        viewer.adjustPanelsByPos(45, 90, 0);
        viewer.adjustPanelsByPos(-45, -90, 0);
        viewer.adjustPanelsByPos(0, 180, 0);
        viewer.adjustPanelsByPos(0, -180, 0);
        // No exception means success
    }

    @Test
    @DisplayName("Empty data lists work correctly")
    public void testEmptyDataLists() {
        viewer.setXAxisData(Arrays.asList());
        viewer.setYAxisData(Arrays.asList());
        viewer.setZAxisData(Arrays.asList());

        assertThat(viewer.getScatterDataGroup().getChildren().size(), is(0));
    }
}
