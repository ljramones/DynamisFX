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
