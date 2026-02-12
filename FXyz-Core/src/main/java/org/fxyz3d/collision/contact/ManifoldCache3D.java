package org.fxyz3d.collision;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Frame-based manifold persistence cache keyed by collision pair.
 */
public final class ManifoldCache3D<T> {

    private final Map<CollisionPair<T>, CachedManifold> cache = new HashMap<>();
    private long frameIndex;

    public void nextFrame() {
        frameIndex++;
    }

    public void put(CollisionPair<T> pair, ContactManifold3D manifold) {
        if (pair == null || manifold == null) {
            throw new IllegalArgumentException("pair and manifold must not be null");
        }
        CachedManifold prior = cache.get(pair);
        WarmStartImpulse warmStart = prior == null ? WarmStartImpulse.ZERO : prior.warmStart();
        cache.put(pair, new CachedManifold(manifold, frameIndex, warmStart));
    }

    public Optional<ContactManifold3D> get(CollisionPair<T> pair) {
        if (pair == null) {
            throw new IllegalArgumentException("pair must not be null");
        }
        CachedManifold cached = cache.get(pair);
        return cached == null ? Optional.empty() : Optional.of(cached.manifold());
    }

    public Optional<WarmStartImpulse> getWarmStart(CollisionPair<T> pair) {
        if (pair == null) {
            throw new IllegalArgumentException("pair must not be null");
        }
        CachedManifold cached = cache.get(pair);
        return cached == null ? Optional.empty() : Optional.of(cached.warmStart());
    }

    public void setWarmStart(CollisionPair<T> pair, WarmStartImpulse warmStart) {
        if (pair == null || warmStart == null) {
            throw new IllegalArgumentException("pair and warmStart must not be null");
        }
        CachedManifold cached = cache.get(pair);
        if (cached == null) {
            return;
        }
        cache.put(pair, new CachedManifold(cached.manifold(), cached.lastUpdatedFrame(), warmStart));
    }

    public void pruneStale(long maxFramesWithoutUpdate) {
        if (maxFramesWithoutUpdate < 0) {
            throw new IllegalArgumentException("maxFramesWithoutUpdate must be >= 0");
        }
        cache.entrySet().removeIf(entry -> (frameIndex - entry.getValue().lastUpdatedFrame()) > maxFramesWithoutUpdate);
    }

    public int size() {
        return cache.size();
    }

    private record CachedManifold(ContactManifold3D manifold, long lastUpdatedFrame, WarmStartImpulse warmStart) {
    }
}
