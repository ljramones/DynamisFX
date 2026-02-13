package org.dynamisfx.collision;

import java.util.function.DoubleConsumer;

/**
 * Fixed-timestep accumulator utility for deterministic stepping.
 */
public final class PhysicsStep3D {

    private final double fixedDtSeconds;
    private int maxSubsteps;
    private double accumulatorSeconds;

    public PhysicsStep3D(double fixedDtSeconds, int maxSubsteps) {
        if (!Double.isFinite(fixedDtSeconds) || fixedDtSeconds <= 0.0) {
            throw new IllegalArgumentException("fixedDtSeconds must be > 0");
        }
        if (maxSubsteps < 1) {
            throw new IllegalArgumentException("maxSubsteps must be >= 1");
        }
        this.fixedDtSeconds = fixedDtSeconds;
        this.maxSubsteps = maxSubsteps;
    }

    public int advance(double frameDeltaSeconds, DoubleConsumer stepConsumer) {
        if (!Double.isFinite(frameDeltaSeconds) || frameDeltaSeconds < 0.0) {
            throw new IllegalArgumentException("frameDeltaSeconds must be finite and >= 0");
        }
        if (stepConsumer == null) {
            throw new IllegalArgumentException("stepConsumer must not be null");
        }
        accumulatorSeconds += frameDeltaSeconds;

        int steps = 0;
        while (accumulatorSeconds >= fixedDtSeconds && steps < maxSubsteps) {
            stepConsumer.accept(fixedDtSeconds);
            accumulatorSeconds -= fixedDtSeconds;
            steps++;
        }
        if (steps == maxSubsteps && accumulatorSeconds > fixedDtSeconds * maxSubsteps) {
            accumulatorSeconds = fixedDtSeconds * maxSubsteps;
        }
        return steps;
    }

    public double fixedDtSeconds() {
        return fixedDtSeconds;
    }

    public int maxSubsteps() {
        return maxSubsteps;
    }

    public void setMaxSubsteps(int maxSubsteps) {
        if (maxSubsteps < 1) {
            throw new IllegalArgumentException("maxSubsteps must be >= 1");
        }
        this.maxSubsteps = maxSubsteps;
    }

    public double accumulatorSeconds() {
        return accumulatorSeconds;
    }

    public void reset() {
        accumulatorSeconds = 0.0;
    }
}
