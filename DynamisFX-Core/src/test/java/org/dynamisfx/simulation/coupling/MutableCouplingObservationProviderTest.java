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

        OptionalDouble distance = provider.distanceMetersToNearestZone("lander-1", List.of(new StubZone()));

        assertTrue(distance.isPresent());
        assertEquals(123.0, distance.orElseThrow(), 1e-9);
        assertTrue(provider.hasActiveContact("lander-1"));
    }

    @Test
    void returnsEmptyDistanceWhenNoZonesOrNoData() {
        MutableCouplingObservationProvider provider = new MutableCouplingObservationProvider();
        provider.setDistanceMeters("lander-1", 123.0);

        assertTrue(provider.distanceMetersToNearestZone("lander-1", List.of()).isEmpty());
        provider.clearDistance("lander-1");
        assertTrue(provider.distanceMetersToNearestZone("lander-1", List.of(new StubZone())).isEmpty());
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
