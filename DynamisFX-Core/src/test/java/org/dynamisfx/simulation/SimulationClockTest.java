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
