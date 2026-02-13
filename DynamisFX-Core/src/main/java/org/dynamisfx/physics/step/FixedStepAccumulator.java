package org.dynamisfx.physics.step;

import java.util.Objects;
import java.util.function.DoubleConsumer;

/**
 * Fixed-timestep accumulator for deterministic simulation stepping.
 */
public final class FixedStepAccumulator {

    private final double fixedStepSeconds;
    private final int maxSubSteps;
    private double accumulatorSeconds;

    public FixedStepAccumulator(double fixedStepSeconds, int maxSubSteps) {
        if (!(fixedStepSeconds > 0.0) || !Double.isFinite(fixedStepSeconds)) {
            throw new IllegalArgumentException("fixedStepSeconds must be > 0 and finite");
        }
        if (maxSubSteps < 1) {
            throw new IllegalArgumentException("maxSubSteps must be >= 1");
        }
        this.fixedStepSeconds = fixedStepSeconds;
        this.maxSubSteps = maxSubSteps;
    }

    public double fixedStepSeconds() {
        return fixedStepSeconds;
    }

    public int maxSubSteps() {
        return maxSubSteps;
    }

    public double accumulatorSeconds() {
        return accumulatorSeconds;
    }

    public void reset() {
        accumulatorSeconds = 0.0;
    }

    public FixedStepResult advance(double frameDtSeconds, DoubleConsumer stepConsumer) {
        if (!(frameDtSeconds >= 0.0) || !Double.isFinite(frameDtSeconds)) {
            throw new IllegalArgumentException("frameDtSeconds must be finite and >= 0");
        }
        Objects.requireNonNull(stepConsumer, "stepConsumer must not be null");

        accumulatorSeconds += frameDtSeconds;

        int steps = 0;
        while (accumulatorSeconds >= fixedStepSeconds && steps < maxSubSteps) {
            stepConsumer.accept(fixedStepSeconds);
            accumulatorSeconds -= fixedStepSeconds;
            steps++;
        }

        double alpha = accumulatorSeconds / fixedStepSeconds;
        if (alpha > 1.0) {
            alpha = 1.0;
        }
        return new FixedStepResult(steps, alpha, accumulatorSeconds);
    }
}
