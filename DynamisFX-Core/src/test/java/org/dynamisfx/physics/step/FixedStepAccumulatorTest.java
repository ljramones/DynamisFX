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
