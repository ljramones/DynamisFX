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

package org.dynamisfx.geometry;

import javafx.geometry.Point3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RayTest {

    private Ray ray;
    private Point3D origin;
    private Point3D direction;

    @BeforeEach
    public void setUp() {
        origin = new Point3D(0, 0, 0);
        direction = new Point3D(1, 0, 0);
        ray = new Ray(origin, direction);
    }

    @Test
    @DisplayName("Constructor sets origin and direction")
    public void testConstruction() {
        assertThat(ray.getOrigin(), is(origin));
        assertThat(ray.getDirection(), is(direction));
    }

    @Test
    @DisplayName("GetPosition returns origin when not projected")
    public void testGetPositionBeforeProjection() {
        assertThat(ray.getPosition(), is(origin));
    }

    @Test
    @DisplayName("Project moves position along direction")
    public void testProject() {
        Point3D projected = ray.project(10.0);

        assertThat(projected.getX(), closeTo(10.0, 0.0001));
        assertThat(projected.getY(), closeTo(0.0, 0.0001));
        assertThat(projected.getZ(), closeTo(0.0, 0.0001));
    }

    @Test
    @DisplayName("Project updates position")
    public void testProjectUpdatesPosition() {
        ray.project(5.0);

        assertThat(ray.getPosition().getX(), closeTo(5.0, 0.0001));
    }

    @Test
    @DisplayName("ReProject changes origin and projects")
    public void testReProject() {
        Point3D newOrigin = new Point3D(10, 10, 10);
        Point3D projected = ray.reProject(newOrigin, 5.0);

        assertThat(ray.getOrigin(), is(newOrigin));
        assertThat(projected.getX(), closeTo(15.0, 0.0001));
        assertThat(projected.getY(), closeTo(10.0, 0.0001));
        assertThat(projected.getZ(), closeTo(10.0, 0.0001));
    }

    @Test
    @DisplayName("SetProject changes origin, direction and projects")
    public void testSetProject() {
        Point3D newOrigin = new Point3D(0, 0, 0);
        Point3D newDirection = new Point3D(0, 1, 0);
        Point3D projected = ray.setProject(newOrigin, newDirection, 10.0);

        assertThat(ray.getOrigin(), is(newOrigin));
        assertThat(ray.getDirection(), is(newDirection));
        assertThat(projected.getY(), closeTo(10.0, 0.0001));
    }

    @Test
    @DisplayName("Project with diagonal direction")
    public void testProjectDiagonal() {
        ray = new Ray(new Point3D(0, 0, 0), new Point3D(1, 1, 1));
        Point3D projected = ray.project(Math.sqrt(3));

        assertThat(projected.getX(), closeTo(1.0, 0.0001));
        assertThat(projected.getY(), closeTo(1.0, 0.0001));
        assertThat(projected.getZ(), closeTo(1.0, 0.0001));
    }

    @Test
    @DisplayName("ToString returns non-null")
    public void testToString() {
        assertThat(ray.toString(), is(notNullValue()));
        assertThat(ray.toString(), containsString("Ray"));
    }
}
