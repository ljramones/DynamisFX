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

import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;

/**
 * Local rigid-body simulation bubble anchored to a global frame.
 */
public interface PhysicsZone {

    ZoneId zoneId();

    ReferenceFrame anchorFrame();

    PhysicsVector3 anchorPosition();

    /**
     * Zone-local orientation in the anchor frame (identity means axis-aligned local frame).
     */
    default PhysicsQuaternion anchorOrientation() {
        return PhysicsQuaternion.IDENTITY;
    }

    double radiusMeters();

    RigidBodyWorld world();
}
