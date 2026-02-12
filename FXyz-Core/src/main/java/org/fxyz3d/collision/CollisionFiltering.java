package org.fxyz3d.collision;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Filter utilities for broad-phase candidate pairs.
 */
public final class CollisionFiltering {

    private CollisionFiltering() {
    }

    public static <T> Set<FilteredCollisionPair<T>> filterPairs(
            Set<CollisionPair<T>> candidates,
            Function<T, CollisionFilter> filterProvider) {
        if (candidates == null || filterProvider == null) {
            throw new IllegalArgumentException("candidates and filterProvider must not be null");
        }
        Set<FilteredCollisionPair<T>> result = new LinkedHashSet<>();
        for (CollisionPair<T> pair : candidates) {
            if (pair == null) {
                continue;
            }
            CollisionFilter left = safeFilter(filterProvider.apply(pair.first()));
            CollisionFilter right = safeFilter(filterProvider.apply(pair.second()));
            if (left.canInteract(right)) {
                result.add(new FilteredCollisionPair<>(pair, left.responseEnabledWith(right)));
            }
        }
        return result;
    }

    private static CollisionFilter safeFilter(CollisionFilter filter) {
        return filter == null ? CollisionFilter.DEFAULT : filter;
    }
}
