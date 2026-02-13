package org.dynamisfx.collision;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Utility for applying narrow-phase checks to broad-phase candidate pairs.
 */
public final class CollisionPipeline {

    private CollisionPipeline() {
    }

    public static <T> Set<CollisionPair<T>> findCollisions(
            Set<CollisionPair<T>> candidates,
            BiPredicate<T, T> narrowPhase) {
        if (candidates == null || narrowPhase == null) {
            throw new IllegalArgumentException("candidates and narrowPhase must not be null");
        }
        Set<CollisionPair<T>> collisions = new LinkedHashSet<>();
        for (CollisionPair<T> pair : candidates) {
            if (pair == null) {
                continue;
            }
            if (narrowPhase.test(pair.first(), pair.second())) {
                collisions.add(pair);
            }
        }
        return collisions;
    }
}
