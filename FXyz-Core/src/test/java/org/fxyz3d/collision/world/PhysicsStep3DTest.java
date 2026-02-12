package org.fxyz3d.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class PhysicsStep3DTest {

    @Test
    void advancesInFixedSubsteps() {
        PhysicsStep3D stepper = new PhysicsStep3D(0.01, 8);
        AtomicInteger calls = new AtomicInteger();

        int steps = stepper.advance(0.025, dt -> calls.incrementAndGet());
        assertEquals(2, steps);
        assertEquals(2, calls.get());
        assertEquals(0.005, stepper.accumulatorSeconds(), 1e-9);
    }

    @Test
    void deterministicAcrossSameInputSequence() {
        double[] deltas = new double[] {0.016, 0.017, 0.015, 0.016, 0.020};
        int totalA = runSequence(deltas);
        int totalB = runSequence(deltas);
        assertEquals(totalA, totalB);
    }

    private static int runSequence(double[] deltas) {
        PhysicsStep3D stepper = new PhysicsStep3D(0.01, 8);
        AtomicInteger calls = new AtomicInteger();
        for (double delta : deltas) {
            stepper.advance(delta, dt -> calls.incrementAndGet());
        }
        return calls.get();
    }
}
