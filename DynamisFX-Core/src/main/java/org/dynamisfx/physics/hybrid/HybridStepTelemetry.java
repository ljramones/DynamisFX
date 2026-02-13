package org.dynamisfx.physics.hybrid;

/**
 * Per-step timing and handoff counters for profiling/debugging.
 */
public record HybridStepTelemetry(
        double simulationTimeSeconds,
        double dtSeconds,
        long orbitalStepNanos,
        long generalStepNanos,
        long handoffNanos,
        int linkCount,
        int handoffCount,
        int rejectedHandoffs) {
}
