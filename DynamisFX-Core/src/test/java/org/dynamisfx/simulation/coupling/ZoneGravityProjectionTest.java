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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class ZoneGravityProjectionTest {

    @Test
    void projectsSphericalGravityIntoLocalFrame() {
        SphericalTangentFrame frame = SphericalTangentFrameBuilder.fromGeodetic(0.0, 0.0, 0.0, 1000.0);
        PhysicsZone zone = new StubZone(frame.anchorPosition(), frame.anchorOrientation());
        PhysicsVector3 gravity = ZoneGravityProjection.projectSphericalGravity(zone, 9.81);
        assertEquals(0.0, gravity.x(), 1e-9);
        assertEquals(0.0, gravity.y(), 1e-9);
        assertEquals(-9.81, gravity.z(), 1e-9);
    }

    private record StubZone(PhysicsVector3 anchorPosition, PhysicsQuaternion anchorOrientation) implements PhysicsZone {

        @Override
        public ZoneId zoneId() {
            return new ZoneId("zone-a");
        }

        @Override
        public ReferenceFrame anchorFrame() {
            return ReferenceFrame.WORLD;
        }

        @Override
        public double radiusMeters() {
            return 1000.0;
        }

        @Override
        public RigidBodyWorld world() {
            return null;
        }
    }
}
