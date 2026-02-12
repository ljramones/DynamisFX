package org.fxyz3d.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class CollisionPipelineTest {

    @Test
    void filtersCandidatesWithNarrowPhase() {
        CollisionPair<String> ab = new CollisionPair<>("a", "b");
        CollisionPair<String> ac = new CollisionPair<>("a", "c");

        Set<CollisionPair<String>> collisions = CollisionPipeline.findCollisions(
                Set.of(ab, ac),
                (left, right) -> ("a".equals(left) && "b".equals(right))
                        || ("b".equals(left) && "a".equals(right)));

        assertEquals(1, collisions.size());
        assertTrue(collisions.contains(ab));
    }
}
