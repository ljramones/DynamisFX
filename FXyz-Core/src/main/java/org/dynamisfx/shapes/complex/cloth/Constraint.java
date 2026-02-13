/**
 * Constraint.java
 *
 * Copyright (c) 2013-2016, F(X)yz
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
