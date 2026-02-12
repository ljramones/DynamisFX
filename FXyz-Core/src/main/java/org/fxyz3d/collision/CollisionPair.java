package org.fxyz3d.collision;

import java.util.Objects;

/**
 * Unordered candidate pair of colliders.
 */
public final class CollisionPair<T> {

    private final T first;
    private final T second;

    public CollisionPair(T first, T second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("pair values must not be null");
        }
        if (first == second) {
            throw new IllegalArgumentException("pair values must be different");
        }
        this.first = first;
        this.second = second;
    }

    public T first() {
        return first;
    }

    public T second() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CollisionPair<?> other)) {
            return false;
        }
        return (Objects.equals(first, other.first) && Objects.equals(second, other.second))
                || (Objects.equals(first, other.second) && Objects.equals(second, other.first));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(first) ^ Objects.hashCode(second);
    }

    @Override
    public String toString() {
        return "CollisionPair[" + first + ", " + second + "]";
    }
}
