package org.dynamisfx.physics.api;

import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Constraint creation definition for physics worlds.
 */
public record PhysicsConstraintDefinition(
        PhysicsConstraintType type,
        PhysicsBodyHandle bodyA,
        PhysicsBodyHandle bodyB,
        PhysicsVector3 anchorWorld,
        PhysicsVector3 axisWorld,
        Double lowerLimit,
        Double upperLimit) {

    public PhysicsConstraintDefinition(
            PhysicsConstraintType type,
            PhysicsBodyHandle bodyA,
            PhysicsBodyHandle bodyB,
            PhysicsVector3 anchorWorld) {
        this(type, bodyA, bodyB, anchorWorld, null, null, null);
    }

    public PhysicsConstraintDefinition {
        if (type == null || bodyA == null || bodyB == null) {
            throw new IllegalArgumentException("type, bodyA and bodyB must not be null");
        }
        if ((type == PhysicsConstraintType.HINGE || type == PhysicsConstraintType.SLIDER) && axisWorld == null) {
            throw new IllegalArgumentException("axisWorld is required for hinge/slider constraints");
        }
        if (lowerLimit != null && !Double.isFinite(lowerLimit)) {
            throw new IllegalArgumentException("lowerLimit must be finite when provided");
        }
        if (upperLimit != null && !Double.isFinite(upperLimit)) {
            throw new IllegalArgumentException("upperLimit must be finite when provided");
        }
        if (lowerLimit != null && upperLimit != null && lowerLimit > upperLimit) {
            throw new IllegalArgumentException("lowerLimit must be <= upperLimit");
        }
    }
}
