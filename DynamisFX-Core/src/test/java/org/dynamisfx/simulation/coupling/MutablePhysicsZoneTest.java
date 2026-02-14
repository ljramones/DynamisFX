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

import java.util.Collection;
import java.util.List;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsCapabilities;
import org.dynamisfx.physics.api.PhysicsConstraintDefinition;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsBodyDefinition;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsRuntimeTuning;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class MutablePhysicsZoneTest {

    @Test
    void updatesAnchorPoseAtRuntime() {
        MutablePhysicsZone zone = new MutablePhysicsZone(
                new ZoneId("zone-a"),
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                1000.0,
                new NoopWorld());

        PhysicsQuaternion z90 = new PhysicsQuaternion(0.0, 0.0, Math.sqrt(0.5), Math.sqrt(0.5));
        zone.updateAnchorPose(new PhysicsVector3(10.0, 2.0, -1.0), z90);

        assertEquals(10.0, zone.anchorPosition().x(), 1e-9);
        assertEquals(2.0, zone.anchorPosition().y(), 1e-9);
        assertEquals(z90.z(), zone.anchorOrientation().z(), 1e-9);
    }

    @Test
    void validatesConstructorInputs() {
        assertThrows(IllegalArgumentException.class, () -> new MutablePhysicsZone(
                new ZoneId("zone-a"),
                ReferenceFrame.WORLD,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                0.0,
                new NoopWorld()));
        assertThrows(NullPointerException.class, () -> new MutablePhysicsZone(
                new ZoneId("zone-a"),
                ReferenceFrame.WORLD,
                null,
                PhysicsQuaternion.IDENTITY,
                1.0,
                new NoopWorld()));
    }

    private static final class NoopWorld implements RigidBodyWorld {

        @Override
        public PhysicsCapabilities capabilities() {
            return new PhysicsCapabilities(false, false, false, false, false);
        }

        @Override
        public PhysicsBodyHandle createBody(PhysicsBodyDefinition definition) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean removeBody(PhysicsBodyHandle handle) {
            return false;
        }

        @Override
        public Collection<PhysicsBodyHandle> bodies() {
            return List.of();
        }

        @Override
        public PhysicsBodyState getBodyState(PhysicsBodyHandle handle) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public void setBodyState(PhysicsBodyHandle handle, PhysicsBodyState state) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public PhysicsConstraintHandle createConstraint(PhysicsConstraintDefinition definition) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean removeConstraint(PhysicsConstraintHandle handle) {
            return false;
        }

        @Override
        public Collection<PhysicsConstraintHandle> constraints() {
            return List.of();
        }

        @Override
        public PhysicsRuntimeTuning runtimeTuning() {
            return new PhysicsRuntimeTuning(4, 1.0, 0.0, 1e-5, 0.1);
        }

        @Override
        public void setRuntimeTuning(PhysicsRuntimeTuning tuning) {
        }

        @Override
        public void step(double dtSeconds) {
        }
    }
}
