package org.fxyz3d.collision;

/**
 * 1D projection interval used by SAT tests.
 */
public record ProjectionInterval(double min, double max) {

    public ProjectionInterval {
        if (!Double.isFinite(min) || !Double.isFinite(max)) {
            throw new IllegalArgumentException("min/max must be finite");
        }
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
    }

    public boolean overlaps(ProjectionInterval other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        return min <= other.max && max >= other.min;
    }

    /**
     * Returns overlap amount, or a negative value when intervals are separated.
     */
    public double overlapDepth(ProjectionInterval other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        return Math.min(max, other.max) - Math.max(min, other.min);
    }
}
