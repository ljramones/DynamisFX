package org.dynamisfx.collision;

/**
 * Cached scalar impulses used for warm starting iterative solvers.
 */
public record WarmStartImpulse(double normalImpulse, double tangentImpulse) {

    public static final WarmStartImpulse ZERO = new WarmStartImpulse(0.0, 0.0);

    public WarmStartImpulse {
        if (!Double.isFinite(normalImpulse) || !Double.isFinite(tangentImpulse)) {
            throw new IllegalArgumentException("impulses must be finite");
        }
    }
}
