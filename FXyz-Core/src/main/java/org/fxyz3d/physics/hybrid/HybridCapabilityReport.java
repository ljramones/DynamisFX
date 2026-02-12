package org.fxyz3d.physics.hybrid;

/**
 * Captures capability-gate results for the registered worlds.
 */
public record HybridCapabilityReport(
        HybridCapabilityPolicy policy,
        boolean generalSupportsRigidBodies,
        boolean orbitalSupportsNBody,
        boolean passed,
        String message) {
}
