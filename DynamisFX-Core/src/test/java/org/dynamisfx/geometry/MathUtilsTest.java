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

package org.dynamisfx.geometry;

import javafx.geometry.Point3D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MathUtilsTest {

    @Test
    @DisplayName("Constants are defined correctly")
    public void testConstants() {
        assertThat(MathUtils.PI, closeTo(Math.PI, 0.0001));
        assertThat(MathUtils.TWO_PI, closeTo(2 * Math.PI, 0.0001));
        assertThat(MathUtils.HALF_PI, closeTo(Math.PI / 2, 0.0001));
        assertThat(MathUtils.TAU, closeTo(2 * Math.PI, 0.0001));
        assertThat(MathUtils.DEG_TO_RAD, closeTo(Math.PI / 180, 0.0001));
        assertThat(MathUtils.RAD_TO_DEG, closeTo(180 / Math.PI, 0.0001));
    }

    @Test
    @DisplayName("isWithinEpsilon returns true for close values")
    public void testIsWithinEpsilonTrue() {
        assertThat(MathUtils.isWithinEpsilon(1.0, 1.00001, 0.001), is(true));
    }

    @Test
    @DisplayName("isWithinEpsilon returns false for distant values")
    public void testIsWithinEpsilonFalse() {
        assertThat(MathUtils.isWithinEpsilon(1.0, 2.0, 0.001), is(false));
    }

    @Test
    @DisplayName("isWithinEpsilon with default tolerance")
    public void testIsWithinEpsilonDefault() {
        assertThat(MathUtils.isWithinEpsilon(1.0, 1.00001), is(true));
        assertThat(MathUtils.isWithinEpsilon(1.0, 1.1), is(false));
    }

    @Test
    @DisplayName("isPowerOfTwo returns true for powers of two")
    public void testIsPowerOfTwoTrue() {
        assertThat(MathUtils.isPowerOfTwo(1), is(true));
        assertThat(MathUtils.isPowerOfTwo(2), is(true));
        assertThat(MathUtils.isPowerOfTwo(4), is(true));
        assertThat(MathUtils.isPowerOfTwo(8), is(true));
        assertThat(MathUtils.isPowerOfTwo(1024), is(true));
    }

    @Test
    @DisplayName("isPowerOfTwo returns false for non-powers of two")
    public void testIsPowerOfTwoFalse() {
        assertThat(MathUtils.isPowerOfTwo(0), is(false));
        assertThat(MathUtils.isPowerOfTwo(3), is(false));
        assertThat(MathUtils.isPowerOfTwo(5), is(false));
        assertThat(MathUtils.isPowerOfTwo(-2), is(false));
    }

    @Test
    @DisplayName("nearestPowerOfTwo rounds up to nearest power")
    public void testNearestPowerOfTwo() {
        assertThat(MathUtils.nearestPowerOfTwo(3), is(4));
        assertThat(MathUtils.nearestPowerOfTwo(5), is(8));
        assertThat(MathUtils.nearestPowerOfTwo(7), is(8));
        assertThat(MathUtils.nearestPowerOfTwo(9), is(16));
    }

    @Test
    @DisplayName("clamp float constrains value to range")
    public void testClampFloat() {
        assertThat(MathUtils.clamp(5.0f, 0.0f, 10.0f), is(5.0f));
        assertThat(MathUtils.clamp(-5.0f, 0.0f, 10.0f), is(0.0f));
        assertThat(MathUtils.clamp(15.0f, 0.0f, 10.0f), is(10.0f));
    }

    @Test
    @DisplayName("clamp double constrains value to range")
    public void testClampDouble() {
        assertThat(MathUtils.clamp(5.0, 0.0, 10.0), is(5.0));
        assertThat(MathUtils.clamp(-5.0, 0.0, 10.0), is(0.0));
        assertThat(MathUtils.clamp(15.0, 0.0, 10.0), is(10.0));
    }

    @Test
    @DisplayName("computeNormal returns normalized cross product")
    public void testComputeNormal() {
        Point3D v1 = new Point3D(0, 0, 0);
        Point3D v2 = new Point3D(1, 0, 0);
        Point3D v3 = new Point3D(0, 1, 0);

        Point3D normal = MathUtils.computeNormal(v1, v2, v3);

        assertThat(normal.magnitude(), closeTo(1.0, 0.0001));
        assertThat(Math.abs(normal.getZ()), closeTo(1.0, 0.0001));
    }

    @Test
    @DisplayName("sphericalToCartesian and back")
    public void testSphericalCartesianConversion() {
        Point3D cartesian = new Point3D(1, 1, 1);
        Point3D spherical = MathUtils.cartesianToSpherical(cartesian);
        Point3D backToCartesian = MathUtils.sphericalToCartesian(spherical);

        assertThat(backToCartesian.getX(), closeTo(cartesian.getX(), 0.0001));
        assertThat(backToCartesian.getY(), closeTo(cartesian.getY(), 0.0001));
        assertThat(backToCartesian.getZ(), closeTo(cartesian.getZ(), 0.0001));
    }
}
