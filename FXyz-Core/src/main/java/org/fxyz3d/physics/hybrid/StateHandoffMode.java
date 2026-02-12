package org.fxyz3d.physics.hybrid;

/**
 * Selects how owner-world state is copied into the follower world.
 */
public enum StateHandoffMode {
    FULL_STATE,
    POSITION_VELOCITY_ONLY
}
