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

package org.dynamisfx.physics.orekit;

import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

/**
 * Minimal frame bridge used by the phase-3 Orekit adapter.
 */
final class OrekitFrameBridge {

    private OrekitFrameBridge() {
    }

    static Frame toOrekitFrame(ReferenceFrame frame) {
        return switch (frame) {
            case EME2000 -> FramesFactory.getEME2000();
            case ICRF -> FramesFactory.getGCRF();
            case WORLD, LOCAL_ENU, ECEF, UNSPECIFIED -> FramesFactory.getEME2000();
        };
    }

    static PhysicsBodyState transformState(
            PhysicsBodyState state,
            ReferenceFrame targetFrame,
            AbsoluteDate date) {
        if (state.referenceFrame() == targetFrame || state.referenceFrame() == ReferenceFrame.UNSPECIFIED) {
            return new PhysicsBodyState(
                    state.position(),
                    state.orientation(),
                    state.linearVelocity(),
                    state.angularVelocity(),
                    targetFrame,
                    state.timestampSeconds());
        }

        Frame source = toOrekitFrame(state.referenceFrame());
        Frame target = toOrekitFrame(targetFrame);
        PVCoordinates transformed = source
                .getTransformTo(target, date)
                .transformPVCoordinates(toPV(state.position(), state.linearVelocity()));

        PhysicsVector3 pos = toPhysics(transformed.getPosition());
        PhysicsVector3 vel = toPhysics(transformed.getVelocity());
        return new PhysicsBodyState(
                pos,
                state.orientation(),
                vel,
                state.angularVelocity(),
                targetFrame,
                state.timestampSeconds());
    }

    static PVCoordinates toPV(PhysicsVector3 position, PhysicsVector3 velocity) {
        return new PVCoordinates(toVector(position), toVector(velocity));
    }

    static PhysicsVector3 toPhysics(Vector3D vector) {
        return new PhysicsVector3(vector.getX(), vector.getY(), vector.getZ());
    }

    static Vector3D toVector(PhysicsVector3 vector) {
        return new Vector3D(vector.x(), vector.y(), vector.z());
    }
}
