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

package org.dynamisfx.shapes.complex.cloth;

import java.util.stream.IntStream;

/**
 * A constraint that can be solved iteratively to satisfy physical conditions.
 * <p>
 * Constraints are used in physics simulations to maintain relationships between
 * objects, such as distance constraints in cloth simulation.
 * </p>
 *
 * @author Jason Pollastrini aka jdub1581
 */
@FunctionalInterface
public interface Constraint {

    /**
     * Solves this constraint once, adjusting positions to satisfy the constraint.
     */
    void solve();

    /**
     * Solves this constraint multiple times for improved accuracy.
     * <p>
     * Multiple iterations help converge to a more stable solution, especially
     * when many constraints interact. Uses sequential execution to avoid
     * race conditions on shared mutable state.
     * </p>
     *
     * @param iter the number of iterations to perform
     */
    default void solve(int iter) {
        for (int i = 0; i < iter; i++) {
            solve();
        }
    }
}
