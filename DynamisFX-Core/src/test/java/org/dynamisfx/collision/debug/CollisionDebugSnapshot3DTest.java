package org.dynamisfx.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class CollisionDebugSnapshot3DTest {

    @Test
    void buildsSnapshotFromItemsAndEvents() {
        Body a = new Body("a", new Aabb(0, 0, 0, 2, 2, 2));
        Body b = new Body("b", new Aabb(1, 1, 1, 3, 3, 3));
        CollisionPair<Body> pair = new CollisionPair<>(a, b);
        CollisionManifold3D manifold3D = new CollisionManifold3D(1, 0, 0, 0.5);
        ContactManifold3D manifold = new ContactManifold3D(
                manifold3D,
                List.of(new ContactPoint3D(1, 1, 1), new ContactPoint3D(2, 2, 2)));
        CollisionEvent<Body> event = new CollisionEvent<>(pair, CollisionEventType.ENTER, true, manifold);

        CollisionDebugSnapshot3D<Body> snapshot = CollisionDebugSnapshot3D.from(
                List.of(a, b),
                Body::bounds,
                List.of(event));

        assertEquals(2, snapshot.items().size());
        assertEquals(2, snapshot.contacts().size());
        assertEquals(a, snapshot.items().get(0).item());
        assertEquals(CollisionEventType.ENTER, snapshot.contacts().get(0).type());
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () ->
                CollisionDebugSnapshot3D.from(null, Body::bounds, List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                CollisionDebugSnapshot3D.from(List.of(), null, List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                CollisionDebugSnapshot3D.from(List.of(), Body::bounds, null));
    }

    private static final class Body {
        private final String id;
        private final Aabb bounds;

        private Body(String id, Aabb bounds) {
            this.id = id;
            this.bounds = bounds;
        }

        private Aabb bounds() {
            return bounds;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
