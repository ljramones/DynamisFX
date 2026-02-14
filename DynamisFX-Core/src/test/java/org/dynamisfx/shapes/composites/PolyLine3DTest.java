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
 */

package org.dynamisfx.shapes.composites;

import javafx.scene.paint.Color;
import org.dynamisfx.geometry.Point3D;
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

        assertThat(line.getPoints(), is(points));
        assertThat(line.getWidth(), is(2.0f));
        assertThat(line.getColor(), is(Color.RED));
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

        assertThat(line.getPoints(), is(points));
        assertThat(line.getMeshView(), is(notNullValue()));
    }

    @Test
    @DisplayName("MeshView is created")
    public void testMeshViewCreated() {
        List<Point3D> points = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0)
        );

        PolyLine3D line = new PolyLine3D(points, 1.0f, Color.WHITE);

        assertThat(line.getMeshView(), is(notNullValue()));
        assertThat(line.getMeshView().getMesh(), is(notNullValue()));
    }

    @Test
    @DisplayName("Material is set")
    public void testMaterialSet() {
        List<Point3D> points = Arrays.asList(
            new Point3D(0, 0, 0),
            new Point3D(10, 0, 0)
        );

        PolyLine3D line = new PolyLine3D(points, 1.0f, Color.GREEN);

        assertThat(line.getMaterial(), is(notNullValue()));
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
