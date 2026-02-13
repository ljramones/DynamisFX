package org.dynamisfx.simulation.coupling;

/**
 * Listener notified when an object's coupling mode changes.
 */
@FunctionalInterface
public interface CouplingTransitionListener {

    void onTransition(CouplingModeTransitionEvent event);
}
