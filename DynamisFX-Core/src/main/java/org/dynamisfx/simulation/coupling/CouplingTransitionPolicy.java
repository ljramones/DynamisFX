package org.dynamisfx.simulation.coupling;

import java.util.Optional;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Policy hook for evaluating per-object mode transitions during coupling updates.
 */
@FunctionalInterface
public interface CouplingTransitionPolicy {

    /**
     * @return a new mode to apply, or empty when no transition should occur
     */
    Optional<ObjectSimulationMode> evaluate(CouplingTransitionContext context);
}
