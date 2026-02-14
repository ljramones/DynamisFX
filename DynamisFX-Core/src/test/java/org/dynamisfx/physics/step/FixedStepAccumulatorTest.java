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
 */

package org.dynamisfx.physics.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class FixedStepAccumulatorTest {

    @Test
    void advancesInDeterministicFixedSteps() {
        FixedStepAccumulator accumulator = new FixedStepAccumulator(0.02, 8);
        AtomicInteger ticks = new AtomicInteger();

        FixedStepResult first = accumulator.advance(0.05, dt -> ticks.incrementAndGet());

        assertEquals(2, first.stepsExecuted());
        assertEquals(2, ticks.get());
        assertEquals(0.01, first.accumulatedRemainderSeconds(), 1e-9);
        assertEquals(0.5, first.interpolationAlpha(), 1e-9);
    }

    @Test
    void limitsSubStepsPerAdvance() {
        FixedStepAccumulator accumulator = new FixedStepAccumulator(0.01, 3);
        AtomicInteger ticks = new AtomicInteger();

        FixedStepResult result = accumulator.advance(0.10, dt -> ticks.incrementAndGet());

        assertEquals(3, result.stepsExecuted());
        assertEquals(3, ticks.get());
        assertEquals(0.07, result.accumulatedRemainderSeconds(), 1e-9);
        assertEquals(1.0, result.interpolationAlpha(), 1e-9);
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> new FixedStepAccumulator(0.0, 1));
        assertThrows(IllegalArgumentException.class, () -> new FixedStepAccumulator(0.01, 0));
        FixedStepAccumulator accumulator = new FixedStepAccumulator(0.01, 2);
        assertThrows(IllegalArgumentException.class, () -> accumulator.advance(-0.01, dt -> {}));
        assertThrows(NullPointerException.class, () -> accumulator.advance(0.01, null));
    }
}
