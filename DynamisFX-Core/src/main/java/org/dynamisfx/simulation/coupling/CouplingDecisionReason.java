package org.dynamisfx.simulation.coupling;

/**
 * Reason emitted by coupling policy evaluation.
 */
public enum CouplingDecisionReason {
    NO_CHANGE,
    PROMOTE_DISTANCE_THRESHOLD,
    DEMOTE_DISTANCE_THRESHOLD,
    BLOCKED_BY_CONTACT,
    BLOCKED_BY_COOLDOWN,
    MISSING_DISTANCE_OBSERVATION,
    UNSUPPORTED_MODE
}
