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

import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.orbital.OrbitalState;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class ZoneFrameTransformTest {

    @Test
    void convertsOrbitalToLocalAndBack() {
        PhysicsZone zone = new StubZone(new ZoneId("zone-a"), new PhysicsVector3(100.0, -5.0, 20.0), ReferenceFrame.WORLD);
        OrbitalState orbital = new OrbitalState(
                new PhysicsVector3(112.0, -2.0, 26.0),
                new PhysicsVector3(1.0, 2.0, 3.0),
                new PhysicsVector3(0.1, 0.2, 0.3),
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                1.0);

        PhysicsBodyState local = ZoneFrameTransform.orbitalToLocalRigid(orbital, 2.0, zone);
        assertEquals(12.0, local.position().x(), 1e-9);
        assertEquals(3.0, local.position().y(), 1e-9);
        assertEquals(6.0, local.position().z(), 1e-9);

        OrbitalState restored = ZoneFrameTransform.localRigidToOrbital(local, 3.0, zone);
        assertEquals(orbital.position().x(), restored.position().x(), 1e-9);
        assertEquals(orbital.position().y(), restored.position().y(), 1e-9);
        assertEquals(orbital.position().z(), restored.position().z(), 1e-9);
        assertEquals(orbital.angularVelocity().x(), restored.angularVelocity().x(), 1e-9);
        assertEquals(orbital.angularVelocity().y(), restored.angularVelocity().y(), 1e-9);
        assertEquals(orbital.angularVelocity().z(), restored.angularVelocity().z(), 1e-9);
    }

    @Test
    void rejectsMismatchedFrames() {
        PhysicsZone zone = new StubZone(new ZoneId("zone-a"), PhysicsVector3.ZERO, ReferenceFrame.WORLD);
        OrbitalState orbital = new OrbitalState(
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.ECEF,
                1.0);

        assertThrows(IllegalArgumentException.class, () -> ZoneFrameTransform.orbitalToLocalRigid(orbital, 1.0, zone));
    }

    @Test
    void rejectsUnspecifiedInputFrame() {
        PhysicsZone zone = new StubZone(new ZoneId("zone-a"), PhysicsVector3.ZERO, ReferenceFrame.WORLD);
        PhysicsBodyState local = new PhysicsBodyState(
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                ReferenceFrame.UNSPECIFIED,
                1.0);
        assertThrows(IllegalArgumentException.class, () -> ZoneFrameTransform.localRigidToOrbital(local, 2.0, zone));
    }

    @Test
    void appliesAnchorOrientationForPositionVelocityAndOrientation() {
        // 90 deg around +Z: local +X maps to global +Y.
        PhysicsQuaternion z90 = new PhysicsQuaternion(0.0, 0.0, Math.sqrt(0.5), Math.sqrt(0.5));
        PhysicsZone zone = new RotatedZone(new ZoneId("zone-r"), new PhysicsVector3(10.0, 20.0, 0.0), z90);
        OrbitalState orbital = new OrbitalState(
                new PhysicsVector3(10.0, 21.0, 0.0),
                new PhysicsVector3(0.0, 2.0, 0.0),
                new PhysicsVector3(0.0, 4.0, 0.0),
                z90,
                ReferenceFrame.WORLD,
                1.0);

        PhysicsBodyState local = ZoneFrameTransform.orbitalToLocalRigid(orbital, 2.0, zone);
        assertEquals(1.0, local.position().x(), 1e-9);
        assertEquals(0.0, local.position().y(), 1e-9);
        assertEquals(2.0, local.linearVelocity().x(), 1e-9);
        assertEquals(0.0, local.linearVelocity().y(), 1e-9);
        assertEquals(4.0, local.angularVelocity().x(), 1e-9);
        assertEquals(0.0, local.angularVelocity().y(), 1e-9);
        assertEquals(PhysicsQuaternion.IDENTITY.x(), local.orientation().x(), 1e-9);
        assertEquals(PhysicsQuaternion.IDENTITY.y(), local.orientation().y(), 1e-9);
        assertEquals(PhysicsQuaternion.IDENTITY.z(), local.orientation().z(), 1e-9);
        assertEquals(PhysicsQuaternion.IDENTITY.w(), local.orientation().w(), 1e-9);

        OrbitalState restored = ZoneFrameTransform.localRigidToOrbital(local, 3.0, zone);
        assertEquals(orbital.position().x(), restored.position().x(), 1e-9);
        assertEquals(orbital.position().y(), restored.position().y(), 1e-9);
        assertEquals(orbital.linearVelocity().x(), restored.linearVelocity().x(), 1e-9);
        assertEquals(orbital.linearVelocity().y(), restored.linearVelocity().y(), 1e-9);
        assertEquals(orbital.angularVelocity().x(), restored.angularVelocity().x(), 1e-9);
        assertEquals(orbital.angularVelocity().y(), restored.angularVelocity().y(), 1e-9);
        assertEquals(orbital.orientation().x(), restored.orientation().x(), 1e-9);
        assertEquals(orbital.orientation().y(), restored.orientation().y(), 1e-9);
        assertEquals(orbital.orientation().z(), restored.orientation().z(), 1e-9);
        assertEquals(orbital.orientation().w(), restored.orientation().w(), 1e-9);
    }

    @Test
    void rejectsZeroNormAnchorOrientation() {
        PhysicsZone zone = new RotatedZone(
                new ZoneId("zone-zero"),
                PhysicsVector3.ZERO,
                new PhysicsQuaternion(0.0, 0.0, 0.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> ZoneFrameTransform.globalToLocalPosition(PhysicsVector3.ZERO, zone));
    }

    private record StubZone(ZoneId zoneId, PhysicsVector3 anchorPosition, ReferenceFrame anchorFrame) implements PhysicsZone {

        @Override
        public double radiusMeters() {
            return 1000.0;
        }

        @Override
        public RigidBodyWorld world() {
            return null;
        }
    }

    private record RotatedZone(ZoneId zoneId, PhysicsVector3 anchorPosition, PhysicsQuaternion anchorOrientation)
            implements PhysicsZone {

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
