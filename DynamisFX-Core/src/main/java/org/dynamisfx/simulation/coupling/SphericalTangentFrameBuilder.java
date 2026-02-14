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
 * Builds local tangent frame anchor poses for spherical bodies.
 */
public final class SphericalTangentFrameBuilder {

    private SphericalTangentFrameBuilder() {
    }

    public static SphericalTangentFrame fromGeodetic(
            double latitudeRadians,
            double longitudeRadians,
            double altitudeMeters,
            double bodyRadiusMeters) {
        if (!Double.isFinite(latitudeRadians) || latitudeRadians < -Math.PI / 2.0 || latitudeRadians > Math.PI / 2.0) {
            throw new IllegalArgumentException("latitudeRadians must be finite and within [-pi/2, pi/2]");
        }
        if (!Double.isFinite(longitudeRadians)) {
            throw new IllegalArgumentException("longitudeRadians must be finite");
        }
        if (!Double.isFinite(altitudeMeters)) {
            throw new IllegalArgumentException("altitudeMeters must be finite");
        }
        if (!Double.isFinite(bodyRadiusMeters) || bodyRadiusMeters <= 0.0) {
            throw new IllegalArgumentException("bodyRadiusMeters must be finite and > 0");
        }

        double clat = Math.cos(latitudeRadians);
        double slat = Math.sin(latitudeRadians);
        double clon = Math.cos(longitudeRadians);
        double slon = Math.sin(longitudeRadians);
        double radius = bodyRadiusMeters + altitudeMeters;

        PhysicsVector3 position = new PhysicsVector3(
                radius * clat * clon,
                radius * clat * slon,
                radius * slat);

        // Local axes in global frame: x=east, y=north, z=up
        PhysicsVector3 east = new PhysicsVector3(-slon, clon, 0.0);
        PhysicsVector3 north = new PhysicsVector3(-slat * clon, -slat * slon, clat);
        PhysicsVector3 up = new PhysicsVector3(clat * clon, clat * slon, slat);
        PhysicsQuaternion orientation = quaternionFromAxes(east, north, up);

        return new SphericalTangentFrame(position, orientation, latitudeRadians, longitudeRadians, altitudeMeters);
    }

    public static SphericalTangentFrame fromCartesian(PhysicsVector3 position, double bodyRadiusMeters) {
        if (position == null) {
            throw new IllegalArgumentException("position must not be null");
        }
        if (!Double.isFinite(bodyRadiusMeters) || bodyRadiusMeters <= 0.0) {
            throw new IllegalArgumentException("bodyRadiusMeters must be finite and > 0");
        }
        double r = Math.sqrt((position.x() * position.x()) + (position.y() * position.y()) + (position.z() * position.z()));
        if (!(r > 0.0)) {
            throw new IllegalArgumentException("position norm must be > 0");
        }
        double latitude = Math.asin(position.z() / r);
        double longitude = Math.atan2(position.y(), position.x());
        double altitude = r - bodyRadiusMeters;
        return fromGeodetic(latitude, longitude, altitude, bodyRadiusMeters);
    }

    private static PhysicsQuaternion quaternionFromAxes(PhysicsVector3 xAxis, PhysicsVector3 yAxis, PhysicsVector3 zAxis) {
        // Rotation matrix columns are the local basis vectors in global coordinates.
        double m00 = xAxis.x();
        double m10 = xAxis.y();
        double m20 = xAxis.z();
        double m01 = yAxis.x();
        double m11 = yAxis.y();
        double m21 = yAxis.z();
        double m02 = zAxis.x();
        double m12 = zAxis.y();
        double m22 = zAxis.z();

        double trace = m00 + m11 + m22;
        double x;
        double y;
        double z;
        double w;
        if (trace > 0.0) {
            double s = Math.sqrt(trace + 1.0) * 2.0;
            w = 0.25 * s;
            x = (m21 - m12) / s;
            y = (m02 - m20) / s;
            z = (m10 - m01) / s;
        } else if (m00 > m11 && m00 > m22) {
            double s = Math.sqrt(1.0 + m00 - m11 - m22) * 2.0;
            w = (m21 - m12) / s;
            x = 0.25 * s;
            y = (m01 + m10) / s;
            z = (m02 + m20) / s;
        } else if (m11 > m22) {
            double s = Math.sqrt(1.0 + m11 - m00 - m22) * 2.0;
            w = (m02 - m20) / s;
            x = (m01 + m10) / s;
            y = 0.25 * s;
            z = (m12 + m21) / s;
        } else {
            double s = Math.sqrt(1.0 + m22 - m00 - m11) * 2.0;
            w = (m10 - m01) / s;
            x = (m02 + m20) / s;
            y = (m12 + m21) / s;
            z = 0.25 * s;
        }
        return normalize(new PhysicsQuaternion(x, y, z, w));
    }

    private static PhysicsQuaternion normalize(PhysicsQuaternion q) {
        double n2 = (q.x() * q.x()) + (q.y() * q.y()) + (q.z() * q.z()) + (q.w() * q.w());
        double inv = 1.0 / Math.sqrt(n2);
        return new PhysicsQuaternion(q.x() * inv, q.y() * inv, q.z() * inv, q.w() * inv);
    }
}
