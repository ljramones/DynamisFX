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

package org.dynamisfx.geometry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Vector3DTest {

    @Test
    @DisplayName("Default constructor creates zero vector")
    public void testDefaultConstructor() {
        Vector3D v = new Vector3D();

        assertThat(v.x, is(0.0));
        assertThat(v.y, is(0.0));
        assertThat(v.z, is(0.0));
    }

    @Test
    @DisplayName("Parameterized constructor sets values")
    public void testParameterizedConstructor() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);

        assertThat(v.x, is(1.0));
        assertThat(v.y, is(2.0));
        assertThat(v.z, is(3.0));
    }

    @Test
    @DisplayName("Copy constructor copies values")
    public void testCopyConstructor() {
        Vector3D original = new Vector3D(1.0, 2.0, 3.0);
        Vector3D copy = new Vector3D(original);

        assertThat(copy.x, is(original.x));
        assertThat(copy.y, is(original.y));
        assertThat(copy.z, is(original.z));
    }

    @Test
    @DisplayName("Array constructor sets values")
    public void testArrayConstructor() {
        Vector3D v = new Vector3D(new double[]{1.0, 2.0, 3.0});

        assertThat(v.x, is(1.0));
        assertThat(v.y, is(2.0));
        assertThat(v.z, is(3.0));
    }

    @Test
    @DisplayName("Magnitude calculates correctly")
    public void testMagnitude() {
        Vector3D v = new Vector3D(3.0, 4.0, 0.0);

        assertThat(v.magnitude(), is(5.0));
    }

    @Test
    @DisplayName("MagnitudeSquared calculates correctly")
    public void testMagnitudeSquared() {
        Vector3D v = new Vector3D(3.0, 4.0, 0.0);

        assertThat(v.magnitudeSquared(), is(25.0));
    }

    @Test
    @DisplayName("Add with constant works")
    public void testAddConstant() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        Vector3D result = v.add(1.0);

        assertThat(result.x, is(2.0));
        assertThat(result.y, is(3.0));
        assertThat(result.z, is(4.0));
    }

    @Test
    @DisplayName("Add with vector works")
    public void testAddVector() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, 5.0, 6.0);
        Vector3D result = v1.add(v2);

        assertThat(result.x, is(5.0));
        assertThat(result.y, is(7.0));
        assertThat(result.z, is(9.0));
    }

    @Test
    @DisplayName("Subtract works")
    public void testSubtract() {
        Vector3D v1 = new Vector3D(4.0, 5.0, 6.0);
        Vector3D v2 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D result = v1.sub(v2);

        assertThat(result.x, is(3.0));
        assertThat(result.y, is(3.0));
        assertThat(result.z, is(3.0));
    }

    @Test
    @DisplayName("Multiply works")
    public void testMultiply() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        Vector3D result = v.multiply(2.0);

        assertThat(result.x, is(2.0));
        assertThat(result.y, is(4.0));
        assertThat(result.z, is(6.0));
    }

    @Test
    @DisplayName("Dot product calculates correctly")
    public void testDotProduct() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, 5.0, 6.0);

        assertThat(v1.dotProduct(v2), is(32.0));
    }

    @Test
    @DisplayName("Cross product calculates correctly")
    public void testCrossProduct() {
        Vector3D v1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v2 = new Vector3D(0.0, 1.0, 0.0);
        Vector3D result = v1.crossProduct(v2);

        assertThat(result.x, is(0.0));
        assertThat(result.y, is(0.0));
        assertThat(result.z, is(1.0));
    }

    @Test
    @DisplayName("Distance calculates correctly")
    public void testDistance() {
        Vector3D v1 = new Vector3D(0.0, 0.0, 0.0);
        Vector3D v2 = new Vector3D(3.0, 4.0, 0.0);

        assertThat(v1.distance(v2), is(5.0));
    }

    @Test
    @DisplayName("Negate works")
    public void testNegate() {
        Vector3D v = new Vector3D(1.0, -2.0, 3.0);
        Vector3D result = v.negate();

        assertThat(result.x, is(-1.0));
        assertThat(result.y, is(2.0));
        assertThat(result.z, is(-3.0));
    }

    @Test
    @DisplayName("ToNormal creates unit vector")
    public void testToNormal() {
        Vector3D v = new Vector3D(3.0, 4.0, 0.0);
        Vector3D normal = v.toNormal();

        assertThat(normal.magnitude(), closeTo(1.0, 0.0001));
    }

    @Test
    @DisplayName("Static constants are correct")
    public void testStaticConstants() {
        assertThat(Vector3D.ZERO.magnitude(), is(0.0));
        assertThat(Vector3D.ONE.x, is(1.0));
        assertThat(Vector3D.UP.y, is(1.0));
        assertThat(Vector3D.DOWN.y, is(-1.0));
        assertThat(Vector3D.FORWARD.z, is(1.0));
    }

    @Test
    @DisplayName("Size returns 3")
    public void testSize() {
        Vector3D v = new Vector3D();
        assertThat(v.size(), is(3));
    }

    @Test
    @DisplayName("GetSum calculates correctly")
    public void testGetSum() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        assertThat(v.getSum(), is(6.0));
    }

    @Test
    @DisplayName("GetProduct calculates correctly")
    public void testGetProduct() {
        Vector3D v = new Vector3D(2.0, 3.0, 4.0);
        assertThat(v.getProduct(), is(24.0));
    }

    @Test
    @DisplayName("ToDoubleArray works")
    public void testToDoubleArray() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        double[] arr = v.toDoubleArray();

        assertThat(arr.length, is(3));
        assertThat(arr[0], is(1.0));
        assertThat(arr[1], is(2.0));
        assertThat(arr[2], is(3.0));
    }
}
