package org.dynamisfx.physics.api;

/**
 * Backend capability flags used for feature gating.
 */
public record PhysicsCapabilities(
        boolean supportsRigidBodies,
        boolean supportsNBody,
        boolean supportsJoints,
        boolean supportsContinuousCollisionDetection,
        boolean supportsQueries) {

    public static final PhysicsCapabilities EMPTY = new PhysicsCapabilities(
            false, false, false, false, false);
}
