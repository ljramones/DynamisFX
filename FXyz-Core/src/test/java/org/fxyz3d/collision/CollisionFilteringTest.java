package org.fxyz3d.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CollisionFilteringTest {

    @Test
    void filtersByLayerMaskAndClassifiesResponse() {
        CollisionPair<String> solidPair = new CollisionPair<>("ship", "asteroid");
        CollisionPair<String> triggerPair = new CollisionPair<>("ship", "sensor");
        CollisionPair<String> ignoredPair = new CollisionPair<>("ship", "ui");

        Map<String, CollisionFilter> filters = Map.of(
                "ship", new CollisionFilter(0b0001, 0b0110, CollisionKind.SOLID),
                "asteroid", new CollisionFilter(0b0010, 0b1111, CollisionKind.SOLID),
                "sensor", new CollisionFilter(0b0100, 0b1111, CollisionKind.TRIGGER),
                "ui", new CollisionFilter(0b1000, 0b1000, CollisionKind.SOLID));

        Set<FilteredCollisionPair<String>> result = CollisionFiltering.filterPairs(
                Set.of(solidPair, triggerPair, ignoredPair),
                filters::get);

        assertEquals(2, result.size());
        assertTrue(result.contains(new FilteredCollisionPair<>(solidPair, true)));
        assertTrue(result.contains(new FilteredCollisionPair<>(triggerPair, false)));
        assertFalse(result.contains(new FilteredCollisionPair<>(ignoredPair, false)));
    }

    @Test
    void defaultsFilterWhenProviderReturnsNull() {
        CollisionPair<String> pair = new CollisionPair<>("a", "b");
        Set<FilteredCollisionPair<String>> result = CollisionFiltering.filterPairs(
                Set.of(pair), item -> null);
        assertEquals(1, result.size());
        assertTrue(result.iterator().next().responseEnabled());
    }
}
