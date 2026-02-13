package org.dynamisfx.simulation.coupling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.dynamisfx.simulation.ObjectSimulationMode;
import org.dynamisfx.simulation.rigid.RigidBodyWorld;
import org.junit.jupiter.api.Test;

class DefaultCouplingManagerTest {

    @Test
    void registersAndRemovesZones() {
        DefaultCouplingManager manager = new DefaultCouplingManager();
        ZoneId zoneId = new ZoneId("zone-a");
        PhysicsZone zone = new StubZone(zoneId);

        manager.registerZone(zone);
        assertEquals(1, manager.zones().size());
        assertTrue(manager.removeZone(zoneId));
        assertEquals(0, manager.zones().size());
        assertFalse(manager.removeZone(zoneId));
    }

    @Test
    void replacesZoneWithSameId() {
        DefaultCouplingManager manager = new DefaultCouplingManager();
        ZoneId zoneId = new ZoneId("zone-a");
        manager.registerZone(new StubZone(zoneId));
        manager.registerZone(new StubZone(zoneId));

        assertEquals(1, manager.zones().size());
    }

    @Test
    void tracksModesByObjectId() {
        DefaultCouplingManager manager = new DefaultCouplingManager();
        String objectId = "lander-1";

        assertTrue(manager.modeFor(objectId).isEmpty());
        manager.setMode(objectId, ObjectSimulationMode.ORBITAL_ONLY);
        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, manager.modeFor(objectId).orElseThrow());

        manager.setMode(objectId, ObjectSimulationMode.PHYSICS_ACTIVE);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor(objectId).orElseThrow());
    }

    @Test
    void validatesInputs() {
        DefaultCouplingManager manager = new DefaultCouplingManager();

        assertThrows(NullPointerException.class, () -> new DefaultCouplingManager(null));
        assertThrows(NullPointerException.class, () -> manager.registerZone(null));
        assertThrows(NullPointerException.class, () -> manager.removeZone(null));
        assertThrows(IllegalArgumentException.class, () -> manager.modeFor(" "));
        assertThrows(IllegalArgumentException.class, () -> manager.setMode("", ObjectSimulationMode.ORBITAL_ONLY));
        assertThrows(NullPointerException.class, () -> manager.setMode("id", null));
        assertThrows(IllegalArgumentException.class, () -> manager.lastTransitionTimeSeconds(""));
        assertThrows(IllegalArgumentException.class, () -> manager.update(Double.NaN));
    }

    @Test
    void appliesPolicyTransitionsAndTracksTransitionTime() {
        CouplingTransitionPolicy policy = context -> {
            assertEquals("lander-1", context.objectId());
            assertEquals(ObjectSimulationMode.ORBITAL_ONLY, context.currentMode());
            assertEquals(1, context.zones().size());
            return Optional.of(ObjectSimulationMode.PHYSICS_ACTIVE);
        };
        DefaultCouplingManager manager = new DefaultCouplingManager(policy);
        manager.registerZone(new StubZone(new ZoneId("zone-a")));
        manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);

        manager.update(12.0);

        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());
        assertEquals(12.0, manager.lastTransitionTimeSeconds("lander-1").orElseThrow(), 1e-9);
    }

    @Test
    void doesNotTrackTransitionWhenModeStaysSame() {
        DefaultCouplingManager manager = new DefaultCouplingManager(context -> Optional.of(context.currentMode()));
        manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);

        manager.update(2.0);

        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, manager.modeFor("lander-1").orElseThrow());
        assertTrue(manager.lastTransitionTimeSeconds("lander-1").isEmpty());
    }

    @Test
    void supportsCooldownStylePolicyWithLastTransitionTimestamp() {
        CouplingTransitionPolicy policy = context -> {
            if (context.lastTransitionTimeSeconds() < 0.0) {
                return Optional.of(ObjectSimulationMode.PHYSICS_ACTIVE);
            }
            if (context.simulationTimeSeconds() < context.lastTransitionTimeSeconds() + 5.0) {
                return Optional.empty();
            }
            return Optional.of(ObjectSimulationMode.PHYSICS_ACTIVE);
        };
        DefaultCouplingManager manager = new DefaultCouplingManager(policy);
        manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);

        manager.update(1.0);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());

        manager.setMode("lander-1", ObjectSimulationMode.ORBITAL_ONLY);
        manager.update(3.0);
        assertEquals(ObjectSimulationMode.ORBITAL_ONLY, manager.modeFor("lander-1").orElseThrow());

        manager.update(7.0);
        assertEquals(ObjectSimulationMode.PHYSICS_ACTIVE, manager.modeFor("lander-1").orElseThrow());
    }

    private record StubZone(ZoneId zoneId) implements PhysicsZone {
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
