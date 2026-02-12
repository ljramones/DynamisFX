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

package org.fxyz3d.geometry;

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
