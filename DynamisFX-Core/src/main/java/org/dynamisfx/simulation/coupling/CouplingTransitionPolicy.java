package org.dynamisfx.simulation.coupling;

/**
 * Policy hook for evaluating per-object mode transitions during coupling updates.
 */
@FunctionalInterface
public interface CouplingTransitionPolicy {

    CouplingTransitionDecision evaluate(CouplingTransitionContext context);
}
