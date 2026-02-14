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

import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Helpers for projecting gravity into zone-local coordinates.
 */
public final class ZoneGravityProjection {

    private ZoneGravityProjection() {
    }

    /**
     * Projects spherical-body gravity (toward global origin) into local zone coordinates.
     */
    public static PhysicsVector3 projectSphericalGravity(PhysicsZone zone, double gravityMagnitudeMetersPerSecondSquared) {
        if (zone == null) {
            throw new IllegalArgumentException("zone must not be null");
        }
        if (!Double.isFinite(gravityMagnitudeMetersPerSecondSquared) || gravityMagnitudeMetersPerSecondSquared < 0.0) {
            throw new IllegalArgumentException("gravityMagnitudeMetersPerSecondSquared must be finite and >= 0");
        }
        PhysicsVector3 p = zone.anchorPosition();
        double r2 = (p.x() * p.x()) + (p.y() * p.y()) + (p.z() * p.z());
        if (!(r2 > 0.0)) {
            // fallback to local -Z if anchor is at origin
            return new PhysicsVector3(0.0, 0.0, -gravityMagnitudeMetersPerSecondSquared);
        }
        double invR = 1.0 / Math.sqrt(r2);
        PhysicsVector3 globalDown = new PhysicsVector3(-p.x() * invR, -p.y() * invR, -p.z() * invR);
        PhysicsQuaternion inv = inverseNormalized(zone.anchorOrientation());
        PhysicsVector3 localDownDir = rotate(inv, globalDown);
        return new PhysicsVector3(
                localDownDir.x() * gravityMagnitudeMetersPerSecondSquared,
                localDownDir.y() * gravityMagnitudeMetersPerSecondSquared,
                localDownDir.z() * gravityMagnitudeMetersPerSecondSquared);
    }

    private static PhysicsQuaternion inverseNormalized(PhysicsQuaternion q) {
        double n2 = (q.x() * q.x()) + (q.y() * q.y()) + (q.z() * q.z()) + (q.w() * q.w());
        double invN = 1.0 / Math.sqrt(n2);
        return new PhysicsQuaternion(-q.x() * invN, -q.y() * invN, -q.z() * invN, q.w() * invN);
    }

    private static PhysicsVector3 rotate(PhysicsQuaternion rotation, PhysicsVector3 vector) {
        PhysicsQuaternion v = new PhysicsQuaternion(vector.x(), vector.y(), vector.z(), 0.0);
        PhysicsQuaternion result = multiply(multiply(rotation, v), conjugate(rotation));
        return new PhysicsVector3(result.x(), result.y(), result.z());
    }

    private static PhysicsQuaternion conjugate(PhysicsQuaternion q) {
        return new PhysicsQuaternion(-q.x(), -q.y(), -q.z(), q.w());
    }

    private static PhysicsQuaternion multiply(PhysicsQuaternion a, PhysicsQuaternion b) {
        return new PhysicsQuaternion(
                (a.w() * b.x()) + (a.x() * b.w()) + (a.y() * b.z()) - (a.z() * b.y()),
                (a.w() * b.y()) - (a.x() * b.z()) + (a.y() * b.w()) + (a.z() * b.x()),
                (a.w() * b.z()) + (a.x() * b.y()) - (a.y() * b.x()) + (a.z() * b.w()),
                (a.w() * b.w()) - (a.x() * b.x()) - (a.y() * b.y()) - (a.z() * b.z()));
    }
}
