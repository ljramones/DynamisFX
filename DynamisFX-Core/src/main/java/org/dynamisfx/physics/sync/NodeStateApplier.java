package org.dynamisfx.physics.sync;

import org.dynamisfx.physics.model.PhysicsBodyState;

/**
 * Applies a physics body state to a scene node abstraction.
 */
@FunctionalInterface
public interface NodeStateApplier<N> {

    void apply(N node, PhysicsBodyState state);
}
