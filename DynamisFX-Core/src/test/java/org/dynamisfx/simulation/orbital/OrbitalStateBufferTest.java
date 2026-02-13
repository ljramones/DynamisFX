package org.dynamisfx.simulation.orbital;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;

class OrbitalStateBufferTest {

    @Test
    void storesAndSnapshotsStates() {
        OrbitalStateBuffer buffer = new OrbitalStateBuffer();
        OrbitalState state = new OrbitalState(
                new PhysicsVector3(1.0, 2.0, 3.0),
                new PhysicsVector3(4.0, 5.0, 6.0),
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                7.0);

        buffer.put("lander-1", state);

        assertEquals(state, buffer.get("lander-1").orElseThrow());
        assertEquals(state, buffer.snapshot().get("lander-1"));
        assertTrue(buffer.remove("lander-1"));
        assertFalse(buffer.remove("lander-1"));
    }

    @Test
    void validatesInputs() {
        OrbitalStateBuffer buffer = new OrbitalStateBuffer();
        OrbitalState state = new OrbitalState(
                PhysicsVector3.ZERO,
                PhysicsVector3.ZERO,
                PhysicsQuaternion.IDENTITY,
                ReferenceFrame.WORLD,
                0.0);

        assertThrows(IllegalArgumentException.class, () -> buffer.put("", state));
        assertThrows(IllegalArgumentException.class, () -> buffer.put("ok", null));
        assertThrows(IllegalArgumentException.class, () -> buffer.get(" "));
        assertThrows(IllegalArgumentException.class, () -> buffer.remove(null));
    }
}
