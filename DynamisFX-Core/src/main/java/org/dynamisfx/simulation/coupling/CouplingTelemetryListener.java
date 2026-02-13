package org.dynamisfx.simulation.coupling;

/**
 * Observer for coupling telemetry events.
 */
@FunctionalInterface
public interface CouplingTelemetryListener {

    void onTelemetry(CouplingTelemetryEvent event);
}
