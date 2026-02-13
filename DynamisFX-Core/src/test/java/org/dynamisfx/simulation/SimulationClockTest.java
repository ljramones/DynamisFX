package org.dynamisfx.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimulationClockTest {

    @Test
    void advancesWithScaleWhenUnpaused() {
        SimulationClock clock = new SimulationClock(10.0, 2.0, false);

        double time = clock.advance(0.5);

        assertEquals(11.0, time, 1e-9);
        assertEquals(11.0, clock.simulationTimeSeconds(), 1e-9);
    }

    @Test
    void doesNotAdvanceWhenPaused() {
        SimulationClock clock = new SimulationClock();
        clock.setPaused(true);

        clock.advance(1.0);

        assertEquals(0.0, clock.simulationTimeSeconds(), 1e-9);
    }

    @Test
    void validatesInputs() {
        assertThrows(IllegalArgumentException.class, () -> new SimulationClock(0.0, -1.0, false));
        assertThrows(IllegalArgumentException.class, () -> new SimulationClock(Double.NaN, 1.0, false));

        SimulationClock clock = new SimulationClock();
        assertThrows(IllegalArgumentException.class, () -> clock.advance(-0.1));
        assertThrows(IllegalArgumentException.class, () -> clock.setTimeScale(Double.POSITIVE_INFINITY));

        clock.setTimeScale(0.25);
        assertTrue(clock.timeScale() > 0.0);
    }
}
