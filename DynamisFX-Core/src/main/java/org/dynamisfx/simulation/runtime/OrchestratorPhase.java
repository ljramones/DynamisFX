package org.dynamisfx.simulation.runtime;

/**
 * Simulation tick phases emitted by the orchestrator.
 */
public enum OrchestratorPhase {
    ORBITAL,
    COUPLING,
    RIGID,
    PUBLISH
}
