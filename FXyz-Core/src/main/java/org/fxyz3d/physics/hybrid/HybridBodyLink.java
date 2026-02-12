package org.fxyz3d.physics.hybrid;

import org.fxyz3d.physics.api.PhysicsBodyHandle;

/**
 * Maps one logical body across general and orbital worlds.
 */
public record HybridBodyLink(
        PhysicsBodyHandle generalBody,
        PhysicsBodyHandle orbitalBody,
        HybridOwnership ownership,
        StateHandoffMode handoffMode,
        ConflictPolicy conflictPolicy,
        double maxPositionDivergenceMeters) {

    public HybridBodyLink(
            PhysicsBodyHandle generalBody,
            PhysicsBodyHandle orbitalBody,
            HybridOwnership ownership,
            StateHandoffMode handoffMode) {
        this(
                generalBody,
                orbitalBody,
                ownership,
                handoffMode,
                ConflictPolicy.OVERWRITE,
                Double.POSITIVE_INFINITY);
    }

    public HybridBodyLink {
        if (generalBody == null || orbitalBody == null || ownership == null
                || handoffMode == null || conflictPolicy == null) {
            throw new IllegalArgumentException(
                    "generalBody, orbitalBody, ownership, handoffMode and conflictPolicy must not be null");
        }
        if (!(maxPositionDivergenceMeters >= 0.0)
                || (Double.isNaN(maxPositionDivergenceMeters))) {
            throw new IllegalArgumentException("maxPositionDivergenceMeters must be >= 0 and not NaN");
        }
    }
}
