/*
 * Copyright 2024-2026 DynamisFX Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dynamisfx.simulation.coupling;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.BoxShape;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsBodyType;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;

/**
 * Spawns terrain tiles as static bodies from a height sampler.
 */
public final class TerrainPatchSpawner {

    private TerrainPatchSpawner() {
    }

    public static TerrainPatchSpawnResult spawnTiles(
            RigidBodyWorld world,
            ReferenceFrame frame,
            TerrainPatchSpec spec,
            TerrainHeightSampler sampler,
            double timestampSeconds) {
        Objects.requireNonNull(world, "world must not be null");
        Objects.requireNonNull(frame, "frame must not be null");
        Objects.requireNonNull(spec, "spec must not be null");
        Objects.requireNonNull(sampler, "sampler must not be null");
        if (!Double.isFinite(timestampSeconds)) {
            throw new IllegalArgumentException("timestampSeconds must be finite");
        }

        double halfExtent = spec.halfExtentMeters();
        double tileSize = spec.tileSizeMeters();
        double thickness = spec.tileThicknessMeters();
        int tilesPerAxis = Math.max(1, (int) Math.ceil((halfExtent * 2.0) / tileSize));
        double start = -halfExtent + (tileSize * 0.5);

        List<PhysicsBodyHandle> created = new ArrayList<>(tilesPerAxis * tilesPerAxis);
        for (int ix = 0; ix < tilesPerAxis; ix++) {
            double x = start + (ix * tileSize);
            for (int iy = 0; iy < tilesPerAxis; iy++) {
                double y = start + (iy * tileSize);
                double surfaceHeight = sampler.sampleHeightMeters(x, y);
                if (!Double.isFinite(surfaceHeight)) {
                    throw new IllegalArgumentException("sampler returned non-finite height");
                }
                double centerZ = surfaceHeight - (thickness * 0.5);
                PhysicsBodyDefinition tile = new PhysicsBodyDefinition(
                        PhysicsBodyType.STATIC,
                        0.0,
                        new BoxShape(tileSize, tileSize, thickness),
                        new PhysicsBodyState(
                                new PhysicsVector3(x, y, centerZ),
                                PhysicsQuaternion.IDENTITY,
                                PhysicsVector3.ZERO,
                                PhysicsVector3.ZERO,
                                frame,
                                timestampSeconds));
                created.add(world.createBody(tile));
            }
        }
        List<PhysicsBodyHandle> handles = List.copyOf(created);
        return new TerrainPatchSpawnResult(handles.size(), handles);
    }
}
