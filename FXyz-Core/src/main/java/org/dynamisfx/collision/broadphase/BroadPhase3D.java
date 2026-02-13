package org.dynamisfx.collision;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * Broad-phase candidate pair generator for 3D bounds.
 */
public interface BroadPhase3D<T> {

    Set<CollisionPair<T>> findPotentialPairs(Collection<T> items, Function<T, Aabb> aabbProvider);
}
