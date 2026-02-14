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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.OptionalDouble;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class MutableCouplingObservationProviderTest {

    @Test
    void returnsConfiguredDistanceAndContact() {
        MutableCouplingObservationProvider provider = new MutableCouplingObservationProvider();
        provider.setDistanceMeters("lander-1", 123.0);
        provider.setActiveContact("lander-1", true);
        provider.setAltitudeMetersAboveSurface("lander-1", 25.0);

        OptionalDouble distance = provider.distanceMetersToNearestZone("lander-1", List.of(new StubZone()));
        OptionalDouble altitude = provider.altitudeMetersAboveSurface("lander-1", List.of(new StubZone()));

        assertTrue(distance.isPresent());
        assertEquals(123.0, distance.orElseThrow(), 1e-9);
        assertTrue(altitude.isPresent());
        assertEquals(25.0, altitude.orElseThrow(), 1e-9);
        assertTrue(provider.hasActiveContact("lander-1"));
    }

    @Test
    void returnsEmptyDistanceWhenNoZonesOrNoData() {
        MutableCouplingObservationProvider provider = new MutableCouplingObservationProvider();
        provider.setDistanceMeters("lander-1", 123.0);

        assertTrue(provider.distanceMetersToNearestZone("lander-1", List.of()).isEmpty());
        provider.clearDistance("lander-1");
        provider.clearAltitudeMetersAboveSurface("lander-1");
        assertTrue(provider.distanceMetersToNearestZone("lander-1", List.of(new StubZone())).isEmpty());
        assertTrue(provider.altitudeMetersAboveSurface("lander-1", List.of(new StubZone())).isEmpty());
        assertFalse(provider.hasActiveContact("lander-1"));
    }

    @Test
    void validatesInputs() {
        MutableCouplingObservationProvider provider = new MutableCouplingObservationProvider();
        assertThrows(IllegalArgumentException.class, () -> provider.setDistanceMeters("", 1.0));
        assertThrows(IllegalArgumentException.class, () -> provider.setDistanceMeters("id", -1.0));
        assertThrows(IllegalArgumentException.class, () -> provider.distanceMetersToNearestZone(" ", List.of()));
        assertThrows(IllegalArgumentException.class, () -> provider.distanceMetersToNearestZone("id", null));
        assertThrows(IllegalArgumentException.class, () -> provider.setActiveContact(" ", true));
        assertThrows(IllegalArgumentException.class, () -> provider.hasActiveContact(""));
        assertThrows(IllegalArgumentException.class, () -> provider.setAltitudeMetersAboveSurface("", 1.0));
        assertThrows(IllegalArgumentException.class, () -> provider.setAltitudeMetersAboveSurface("id", -1.0));
        assertThrows(IllegalArgumentException.class, () -> provider.altitudeMetersAboveSurface(" ", List.of()));
        assertThrows(IllegalArgumentException.class, () -> provider.altitudeMetersAboveSurface("id", null));
    }

    private static final class StubZone implements PhysicsZone {
        @Override
        public ZoneId zoneId() {
            return new ZoneId("zone-a");
        }

        @Override
        public ReferenceFrame anchorFrame() {
            return ReferenceFrame.WORLD;
        }

        @Override
        public PhysicsVector3 anchorPosition() {
            return PhysicsVector3.ZERO;
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
