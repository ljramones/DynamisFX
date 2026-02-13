package org.dynamisfx.simulation.rigid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.ReferenceFrame;
import org.junit.jupiter.api.Test;

class RigidStateBufferTest {

    @Test
    void storesAndSnapshotsStates() {
        RigidStateBuffer buffer = new RigidStateBuffer();
        PhysicsBodyState state = new PhysicsBodyState(
                new PhysicsVector3(1.0, 2.0, 3.0),
                PhysicsQuaternion.IDENTITY,
                new PhysicsVector3(4.0, 5.0, 6.0),
                PhysicsVector3.ZERO,
                ReferenceFrame.WORLD,
                1.0);

        buffer.put("lander-1", state);

        assertTrue(buffer.get("lander-1").isPresent());
        assertEquals(state, buffer.snapshot().get("lander-1"));
    }

    @Test
    void advancesLinearStateAndUpdatesTimestamp() {
        RigidStateBuffer buffer = new RigidStateBuffer();
        buffer.put("lander-1", new PhysicsBodyState(
                new PhysicsVector3(10.0, 0.0, 0.0),
                PhysicsQuaternion.IDENTITY,
                new PhysicsVector3(2.0, -1.0, 0.5),
                PhysicsVector3.ZERO,
                ReferenceFrame.WORLD,
                1.0));

        boolean advanced = buffer.advanceLinear("lander-1", 0.5, 2.0);

        assertTrue(advanced);
        PhysicsBodyState state = buffer.get("lander-1").orElseThrow();
        assertEquals(11.0, state.position().x(), 1e-9);
        assertEquals(-0.5, state.position().y(), 1e-9);
        assertEquals(0.25, state.position().z(), 1e-9);
        assertEquals(2.0, state.timestampSeconds(), 1e-9);
    }

    @Test
    void validatesInputsAndMissingObjectAdvance() {
        RigidStateBuffer buffer = new RigidStateBuffer();

        assertThrows(IllegalArgumentException.class, () -> buffer.put("", PhysicsBodyState.IDENTITY));
        assertThrows(IllegalArgumentException.class, () -> buffer.put("x", null));
        assertThrows(IllegalArgumentException.class, () -> buffer.get(" "));
        assertThrows(IllegalArgumentException.class, () -> buffer.remove(null));
        assertThrows(IllegalArgumentException.class, () -> buffer.advanceLinear("x", -1.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> buffer.advanceLinear("x", 1.0, Double.NaN));
        assertFalse(buffer.advanceLinear("missing", 1.0, 0.0));
    }
}
