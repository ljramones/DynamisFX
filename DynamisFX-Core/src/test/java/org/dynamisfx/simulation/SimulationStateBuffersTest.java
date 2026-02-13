package org.dynamisfx.simulation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dynamisfx.simulation.orbital.OrbitalStateBuffer;
import org.dynamisfx.simulation.rigid.RigidStateBuffer;
import org.junit.jupiter.api.Test;

class SimulationStateBuffersTest {

    @Test
    void createsDefaultBuffers() {
        SimulationStateBuffers buffers = new SimulationStateBuffers();

        assertNotNull(buffers.orbital());
        assertNotNull(buffers.rigid());
    }

    @Test
    void wrapsProvidedBuffers() {
        OrbitalStateBuffer orbital = new OrbitalStateBuffer();
        RigidStateBuffer rigid = new RigidStateBuffer();

        SimulationStateBuffers buffers = new SimulationStateBuffers(orbital, rigid);

        assertSame(orbital, buffers.orbital());
        assertSame(rigid, buffers.rigid());
    }

    @Test
    void validatesConstructorInputs() {
        assertThrows(NullPointerException.class, () -> new SimulationStateBuffers(null, new RigidStateBuffer()));
        assertThrows(NullPointerException.class, () -> new SimulationStateBuffers(new OrbitalStateBuffer(), null));
    }
}
