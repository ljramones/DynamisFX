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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class SphericalTangentFrameBuilderTest {

    @Test
    void buildsExpectedFrameAtEquatorPrimeMeridian() {
        SphericalTangentFrame frame = SphericalTangentFrameBuilder.fromGeodetic(0.0, 0.0, 100.0, 1_000.0);
        assertEquals(1100.0, frame.anchorPosition().x(), 1e-9);
        assertEquals(0.0, frame.anchorPosition().y(), 1e-9);
        assertEquals(0.0, frame.anchorPosition().z(), 1e-9);

        PhysicsZone zone = new StubZone(frame.anchorPosition(), frame.anchorOrientation());
        PhysicsVector3 globalEast = ZoneFrameTransform.localToGlobalPosition(new PhysicsVector3(1.0, 0.0, 0.0), zone);
        // local east should align to +Y at lat=0 lon=0.
        assertEquals(frame.anchorPosition().x(), globalEast.x(), 1e-9);
        assertEquals(frame.anchorPosition().y() + 1.0, globalEast.y(), 1e-9);
    }

    @Test
    void buildsFromCartesianAndMatchesAltitude() {
        SphericalTangentFrame frame = SphericalTangentFrameBuilder.fromCartesian(new PhysicsVector3(0.0, 0.0, 1_100.0), 1_000.0);
        assertEquals(Math.PI / 2.0, frame.latitudeRadians(), 1e-9);
        assertEquals(100.0, frame.altitudeMeters(), 1e-9);
    }

    @Test
    void validatesInputs() {
        assertThrows(IllegalArgumentException.class, () -> SphericalTangentFrameBuilder.fromGeodetic(2.0, 0.0, 0.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> SphericalTangentFrameBuilder.fromGeodetic(0.0, 0.0, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> SphericalTangentFrameBuilder.fromCartesian(PhysicsVector3.ZERO, 1.0));
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
