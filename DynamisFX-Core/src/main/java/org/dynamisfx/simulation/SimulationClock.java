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

/**
 * Mutable simulation clock that advances according to real delta and time scale.
 */
public final class SimulationClock {

    private double simulationTimeSeconds;
    private double timeScale;
    private boolean paused;

    public SimulationClock() {
        this(0.0, 1.0, false);
    }

    public SimulationClock(double simulationTimeSeconds, double timeScale, boolean paused) {
        if (!Double.isFinite(simulationTimeSeconds)) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite");
        }
        validateTimeScale(timeScale);
        this.simulationTimeSeconds = simulationTimeSeconds;
        this.timeScale = timeScale;
        this.paused = paused;
    }

    public synchronized double simulationTimeSeconds() {
        return simulationTimeSeconds;
    }

    public synchronized double timeScale() {
        return timeScale;
    }

    public synchronized boolean paused() {
        return paused;
    }

    public synchronized void setTimeScale(double timeScale) {
        validateTimeScale(timeScale);
        this.timeScale = timeScale;
    }

    public synchronized void setPaused(boolean paused) {
        this.paused = paused;
    }

    public synchronized void reset(double simulationTimeSeconds) {
        if (!Double.isFinite(simulationTimeSeconds)) {
            throw new IllegalArgumentException("simulationTimeSeconds must be finite");
        }
        this.simulationTimeSeconds = simulationTimeSeconds;
    }

    /**
     * Advances simulation time by the provided real-world delta in seconds.
     *
     * @return the current simulation time after processing the delta
     */
    public synchronized double advance(double realDeltaSeconds) {
        if (!Double.isFinite(realDeltaSeconds) || realDeltaSeconds < 0.0) {
            throw new IllegalArgumentException("realDeltaSeconds must be finite and >= 0");
        }
        if (!paused) {
            simulationTimeSeconds += realDeltaSeconds * timeScale;
        }
        return simulationTimeSeconds;
    }

    private static void validateTimeScale(double timeScale) {
        if (!Double.isFinite(timeScale) || timeScale < 0.0) {
            throw new IllegalArgumentException("timeScale must be finite and >= 0");
        }
    }
}
