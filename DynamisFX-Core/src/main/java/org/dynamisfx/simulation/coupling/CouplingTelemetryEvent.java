package org.dynamisfx.simulation.coupling;

import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Telemetry event emitted per object update by the coupling manager.
 */
public record CouplingTelemetryEvent(
        double simulationTimeSeconds,
        String objectId,
        ObjectSimulationMode fromMode,
        ObjectSimulationMode toMode,
        boolean transitioned,
        CouplingDecisionReason reason) {
}
