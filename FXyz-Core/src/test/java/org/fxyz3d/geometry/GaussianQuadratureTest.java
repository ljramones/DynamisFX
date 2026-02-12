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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GaussianQuadratureTest {

    @Test
    @DisplayName("Default constructor creates 5-point quadrature")
    public void testDefaultConstruction() {
        GaussianQuadrature gq = new GaussianQuadrature();
        // Integrate x^2 from -1 to 1, should be 2/3
        double result = gq.NIntegrate(x -> x * x);
        assertThat(result, closeTo(2.0 / 3.0, 0.0001));
    }

    @Test
    @DisplayName("Integrate constant function")
    public void testIntegrateConstant() {
        GaussianQuadrature gq = new GaussianQuadrature(5, 0, 1);
        double result = gq.NIntegrate(x -> 1.0);
        assertThat(result, closeTo(1.0, 0.0001));
    }

    @Test
    @DisplayName("Integrate linear function")
    public void testIntegrateLinear() {
        GaussianQuadrature gq = new GaussianQuadrature(5, 0, 1);
        // Integral of x from 0 to 1 is 0.5
        double result = gq.NIntegrate(x -> x);
        assertThat(result, closeTo(0.5, 0.0001));
    }

    @Test
    @DisplayName("Integrate sin(x) from 0 to PI")
    public void testIntegrateSin() {
        GaussianQuadrature gq = new GaussianQuadrature(7, 0, Math.PI);
        // Integral of sin(x) from 0 to PI is 2
        double result = gq.NIntegrate(Math::sin);
        assertThat(result, closeTo(2.0, 0.0001));
    }

    @Test
    @DisplayName("2-point quadrature works")
    public void test2PointQuadrature() {
        GaussianQuadrature gq = new GaussianQuadrature(2, -1, 1);
        double result = gq.NIntegrate(x -> x * x);
        assertThat(result, closeTo(2.0 / 3.0, 0.001));
    }

    @Test
    @DisplayName("3-point quadrature works")
    public void test3PointQuadrature() {
        GaussianQuadrature gq = new GaussianQuadrature(3, -1, 1);
        double result = gq.NIntegrate(x -> x * x);
        assertThat(result, closeTo(2.0 / 3.0, 0.0001));
    }

    @Test
    @DisplayName("4-point quadrature works")
    public void test4PointQuadrature() {
        GaussianQuadrature gq = new GaussianQuadrature(4, -1, 1);
        double result = gq.NIntegrate(x -> x * x);
        assertThat(result, closeTo(2.0 / 3.0, 0.0001));
    }

    @Test
    @DisplayName("6-point quadrature works")
    public void test6PointQuadrature() {
        GaussianQuadrature gq = new GaussianQuadrature(6, -1, 1);
        double result = gq.NIntegrate(x -> x * x);
        assertThat(result, closeTo(2.0 / 3.0, 0.0001));
    }

    @Test
    @DisplayName("Integrate polynomial exactly")
    public void testIntegratePolynomial() {
        // Gaussian quadrature with n points is exact for polynomials up to degree 2n-1
        GaussianQuadrature gq = new GaussianQuadrature(3, 0, 2);
        // Integral of x^3 from 0 to 2 is 4
        double result = gq.NIntegrate(x -> x * x * x);
        assertThat(result, closeTo(4.0, 0.0001));
    }

    @Test
    @DisplayName("Custom range works")
    public void testCustomRange() {
        GaussianQuadrature gq = new GaussianQuadrature(5, 0, 10);
        // Integral of 1 from 0 to 10 is 10
        double result = gq.NIntegrate(x -> 1.0);
        assertThat(result, closeTo(10.0, 0.0001));
    }
}
