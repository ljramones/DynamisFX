package org.fxyz3d.physics.model;

/**
 * Marker interface for engine-agnostic body shapes.
 */
public sealed interface PhysicsShape permits BoxShape, SphereShape, CapsuleShape {
}
