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
