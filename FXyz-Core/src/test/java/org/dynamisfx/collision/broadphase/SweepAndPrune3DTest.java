package org.dynamisfx.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SweepAndPrune3DTest {

    @Test
    void findsPairsThatOverlapOnAllAxes() {
        record Body(String id, Aabb bounds) {
        }

        Body a = new Body("a", new Aabb(0, 0, 0, 2, 2, 2));
        Body b = new Body("b", new Aabb(1, 1, 1, 3, 3, 3));
        Body c = new Body("c", new Aabb(3.1, 0, 0, 4, 1, 1));

        SweepAndPrune3D<Body> sap = new SweepAndPrune3D<>();
        Set<CollisionPair<Body>> pairs = sap.findPotentialPairs(List.of(a, b, c), Body::bounds);

        assertEquals(1, pairs.size());
        assertTrue(pairs.contains(new CollisionPair<>(a, b)));
    }

    @Test
    void touchingBoundsCountAsPotentialPair() {
        record Body(String id, Aabb bounds) {
        }

        Body a = new Body("a", new Aabb(0, 0, 0, 1, 1, 1));
        Body b = new Body("b", new Aabb(1, 0, 0, 2, 1, 1));

        SweepAndPrune3D<Body> sap = new SweepAndPrune3D<>();
        Set<CollisionPair<Body>> pairs = sap.findPotentialPairs(List.of(a, b), Body::bounds);

        assertEquals(1, pairs.size());
        assertTrue(pairs.contains(new CollisionPair<>(a, b)));
    }

    @Test
    void supportsBroadPhaseInterfaceUsage() {
        record Body(String id, Aabb bounds) {
        }

        Body a = new Body("a", new Aabb(0, 0, 0, 1, 1, 1));
        Body b = new Body("b", new Aabb(0.5, 0.5, 0.5, 1.5, 1.5, 1.5));

        BroadPhase3D<Body> broadPhase = new SweepAndPrune3D<>();
        Set<CollisionPair<Body>> pairs = broadPhase.findPotentialPairs(List.of(a, b), Body::bounds);

        assertEquals(1, pairs.size());
    }
}
