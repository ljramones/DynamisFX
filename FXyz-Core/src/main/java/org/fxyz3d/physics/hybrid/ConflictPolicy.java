package org.fxyz3d.physics.hybrid;

/**
 * Defines how state divergence is handled during owner-to-follower handoff.
 */
public enum ConflictPolicy {
    OVERWRITE,
    REJECT_ON_DIVERGENCE
}
