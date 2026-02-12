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

import javafx.scene.paint.Color;
import org.fxyz3d.geometry.Point3D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PolyLine3DTest {

    @Test
    @DisplayName("Ribbon constructor creates polyline")
    public void testRibbonConstruction() {
        List<Point3D> points = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0),
            new Point3D(20, 10, 0)
        );

        PolyLine3D line = new PolyLine3D(points, 2.0f, Color.RED);

        assertThat(line.points, is(points));
        assertThat(line.width, is(2.0f));
        assertThat(line.color, is(Color.RED));
    }

    @Test
    @DisplayName("Triangle tube constructor creates polyline")
    public void testTriangleTubeConstruction() {
        List<Point3D> points = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0),
            new Point3D(20, 10, 0)
        );

        PolyLine3D line = new PolyLine3D(points, 2.0f, Color.BLUE, PolyLine3D.LineType.TRIANGLE);

        assertThat(line.points, is(points));
        assertThat(line.meshView, is(notNullValue()));
    }

    @Test
    @DisplayName("MeshView is created")
    public void testMeshViewCreated() {
        List<Point3D> points = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0)
        );

        PolyLine3D line = new PolyLine3D(points, 1.0f, Color.WHITE);

        assertThat(line.meshView, is(notNullValue()));
        assertThat(line.meshView.getMesh(), is(notNullValue()));
    }

    @Test
    @DisplayName("Material is set")
    public void testMaterialSet() {
        List<Point3D> points = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0)
        );

        PolyLine3D line = new PolyLine3D(points, 1.0f, Color.GREEN);

        assertThat(line.material, is(notNullValue()));
    }

    @Test
    @DisplayName("Group contains children")
    public void testGroupContainsChildren() {
        List<Point3D> points = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0)
        );

        PolyLine3D line = new PolyLine3D(points, 1.0f, Color.WHITE);

        assertThat(line.getChildren().size(), greaterThan(0));
    }
}
