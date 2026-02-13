package org.dynamisfx.physics.model;

/**
 * Body creation definition for backend-neutral world APIs.
 */
public record PhysicsBodyDefinition(
        PhysicsBodyType bodyType,
        double massKg,
        PhysicsShape shape,
        PhysicsBodyState initialState) {

    public PhysicsBodyDefinition {
        if (bodyType == null || shape == null || initialState == null) {
            throw new IllegalArgumentException("bodyType, shape and initialState must not be null");
        }
        if (bodyType == PhysicsBodyType.DYNAMIC) {
            if (!(massKg > 0.0) || !Double.isFinite(massKg)) {
                throw new IllegalArgumentException("dynamic body mass must be > 0 and finite");
            }
        } else if (massKg < 0.0 || !Double.isFinite(massKg)) {
            throw new IllegalArgumentException("massKg must be >= 0 and finite");
        }
    }
}
