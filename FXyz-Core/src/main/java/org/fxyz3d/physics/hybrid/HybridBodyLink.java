package org.fxyz3d.physics.hybrid;

import org.fxyz3d.physics.api.PhysicsBodyHandle;

/**
 * Maps one logical body across general and orbital worlds.
 */
public record HybridBodyLink(
        PhysicsBodyHandle generalBody,
        PhysicsBodyHandle orbitalBody,
        HybridOwnership ownership,
        StateHandoffMode handoffMode) {

    public HybridBodyLink {
        if (generalBody == null || orbitalBody == null || ownership == null || handoffMode == null) {
            throw new IllegalArgumentException("generalBody, orbitalBody, ownership and handoffMode must not be null");
        }
    }
}
