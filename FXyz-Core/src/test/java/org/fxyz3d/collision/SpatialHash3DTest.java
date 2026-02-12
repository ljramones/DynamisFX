package org.fxyz3d.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SpatialHash3DTest {

    @Test
    void constructorRejectsInvalidCellSize() {
        assertThrows(IllegalArgumentException.class, () -> new SpatialHash3D<String>(0));
        assertThrows(IllegalArgumentException.class, () -> new SpatialHash3D<String>(-1));
    }

    @Test
    void potentialPairsIncludeOverlappingBucketsOnly() {
        record Body(String id, Aabb aabb) {
        }

        Body a = new Body("a", new Aabb(0.0, 0.0, 0.0, 0.9, 0.9, 0.9));
        Body b = new Body("b", new Aabb(0.8, 0.0, 0.0, 1.6, 0.9, 0.9));
        Body c = new Body("c", new Aabb(5.0, 5.0, 5.0, 6.0, 6.0, 6.0));

        SpatialHash3D<Body> hash = new SpatialHash3D<>(1.0);
        Set<CollisionPair<Body>> pairs = hash.findPotentialPairs(List.of(a, b, c), Body::aabb);

        assertEquals(1, pairs.size());
        assertTrue(pairs.contains(new CollisionPair<>(a, b)));
    }

    @Test
    void pairSetIsUniqueWhenObjectsShareMultipleCells() {
        record Body(String id, Aabb aabb) {
        }

        Body a = new Body("a", new Aabb(0.0, 0.0, 0.0, 2.2, 2.2, 2.2));
        Body b = new Body("b", new Aabb(1.0, 1.0, 1.0, 3.0, 3.0, 3.0));

        SpatialHash3D<Body> hash = new SpatialHash3D<>(1.0);
        Set<CollisionPair<Body>> pairs = hash.findPotentialPairs(List.of(a, b), Body::aabb);

        assertEquals(1, pairs.size());
        assertTrue(pairs.contains(new CollisionPair<>(a, b)));
    }
}
