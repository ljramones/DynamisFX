package org.dynamisfx.simulation.coupling;

import java.util.Objects;
import java.util.Optional;
import org.dynamisfx.simulation.ObjectSimulationMode;

/**
 * Encapsulates policy decision and reason for telemetry and control flow.
 */
public record CouplingTransitionDecision(Optional<ObjectSimulationMode> nextMode, CouplingDecisionReason reason) {

    public CouplingTransitionDecision {
        Objects.requireNonNull(nextMode, "nextMode must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
    }

    public static CouplingTransitionDecision noChange(CouplingDecisionReason reason) {
        return new CouplingTransitionDecision(Optional.empty(), reason);
    }

    public static CouplingTransitionDecision transitionTo(ObjectSimulationMode nextMode, CouplingDecisionReason reason) {
        return new CouplingTransitionDecision(Optional.of(nextMode), reason);
    }
}
