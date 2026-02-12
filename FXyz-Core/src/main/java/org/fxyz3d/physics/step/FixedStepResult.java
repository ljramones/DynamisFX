package org.fxyz3d.physics.step;

/**
 * Result of a fixed-step accumulator advance.
 */
public record FixedStepResult(
        int stepsExecuted,
        double interpolationAlpha,
        double accumulatedRemainderSeconds) {

    public FixedStepResult {
        if (stepsExecuted < 0) {
            throw new IllegalArgumentException("stepsExecuted must be >= 0");
        }
        if (!Double.isFinite(interpolationAlpha) || interpolationAlpha < 0.0 || interpolationAlpha > 1.0) {
            throw new IllegalArgumentException("interpolationAlpha must be finite in [0,1]");
        }
        if (!Double.isFinite(accumulatedRemainderSeconds) || accumulatedRemainderSeconds < 0.0) {
            throw new IllegalArgumentException("accumulatedRemainderSeconds must be finite and >= 0");
        }
    }
}
