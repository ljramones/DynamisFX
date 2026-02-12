package org.fxyz3d.physics.hybrid;

/**
 * Read-only diagnostics for one registered hybrid link.
 */
public record HybridLinkDiagnostics(
        long linkId,
        HybridBodyLink link,
        boolean enabled,
        long rejectedCount,
        double lastPositionErrorMeters,
        double lastLinearVelocityErrorMetersPerSecond,
        double lastAngularVelocityErrorRadiansPerSecond,
        double lastHandoffTimeSeconds) {
}
