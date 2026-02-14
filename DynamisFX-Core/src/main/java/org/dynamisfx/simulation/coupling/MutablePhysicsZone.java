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

import java.util.Objects;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;

/**
 * Mutable physics-zone implementation with runtime-updatable anchor pose.
 */
public final class MutablePhysicsZone implements PhysicsZone {

    private final ZoneId zoneId;
    private final ReferenceFrame anchorFrame;
    private final double radiusMeters;
    private final RigidBodyWorld world;
    private volatile PhysicsVector3 anchorPosition;
    private volatile PhysicsQuaternion anchorOrientation;

    public MutablePhysicsZone(
            ZoneId zoneId,
            ReferenceFrame anchorFrame,
            PhysicsVector3 anchorPosition,
            PhysicsQuaternion anchorOrientation,
            double radiusMeters,
            RigidBodyWorld world) {
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId must not be null");
        this.anchorFrame = Objects.requireNonNull(anchorFrame, "anchorFrame must not be null");
        this.anchorPosition = Objects.requireNonNull(anchorPosition, "anchorPosition must not be null");
        this.anchorOrientation = Objects.requireNonNull(anchorOrientation, "anchorOrientation must not be null");
        if (!Double.isFinite(radiusMeters) || radiusMeters <= 0.0) {
            throw new IllegalArgumentException("radiusMeters must be finite and > 0");
        }
        this.radiusMeters = radiusMeters;
        this.world = Objects.requireNonNull(world, "world must not be null");
    }

    @Override
    public ZoneId zoneId() {
        return zoneId;
    }

    @Override
    public ReferenceFrame anchorFrame() {
        return anchorFrame;
    }

    @Override
    public PhysicsVector3 anchorPosition() {
        return anchorPosition;
    }

    @Override
    public PhysicsQuaternion anchorOrientation() {
        return anchorOrientation;
    }

    @Override
    public double radiusMeters() {
        return radiusMeters;
    }

    @Override
    public RigidBodyWorld world() {
        return world;
    }

    public void updateAnchorPose(PhysicsVector3 position, PhysicsQuaternion orientation) {
        this.anchorPosition = Objects.requireNonNull(position, "position must not be null");
        this.anchorOrientation = Objects.requireNonNull(orientation, "orientation must not be null");
    }
}
