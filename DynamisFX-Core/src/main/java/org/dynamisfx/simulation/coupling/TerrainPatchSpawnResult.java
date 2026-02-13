package org.dynamisfx.simulation.coupling;

import java.util.List;
import org.dynamisfx.physics.api.PhysicsBodyHandle;

/**
 * Summary of spawned terrain tile bodies.
 */
public record TerrainPatchSpawnResult(
        int tileCount,
        List<PhysicsBodyHandle> tileHandles) {

    public TerrainPatchSpawnResult {
        if (tileCount < 0) {
            throw new IllegalArgumentException("tileCount must be >= 0");
        }
        if (tileHandles == null) {
            throw new IllegalArgumentException("tileHandles must not be null");
        }
    }
}
