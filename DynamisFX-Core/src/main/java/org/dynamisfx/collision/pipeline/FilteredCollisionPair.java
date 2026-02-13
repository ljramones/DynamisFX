package org.dynamisfx.collision;

/**
 * Collision pair annotated with response classification.
 */
public record FilteredCollisionPair<T>(CollisionPair<T> pair, boolean responseEnabled) {

    public FilteredCollisionPair {
        if (pair == null) {
            throw new IllegalArgumentException("pair must not be null");
        }
    }
}
