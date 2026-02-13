package org.dynamisfx.simulation.runtime;

/**
 * Listener for simulation orchestrator phase events.
 */
@FunctionalInterface
public interface SimulationOrchestratorListener {

    void onPhase(OrchestratorPhase phase, double simulationTimeSeconds);
}
