package org.dynamisfx.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ContactGenerator3DTest {

    @Test
    void generatesAabbContactWithAxisManifold() {
        Aabb a = new Aabb(0, 0, 0, 2, 2, 2);
        Aabb b = new Aabb(1.5, 0.5, 0.5, 3, 1.5, 1.5);

        ContactManifold3D manifold = ContactGenerator3D.generate(a, b).orElseThrow();
        CollisionManifold3D m = manifold.manifold();

        assertEquals(1.0, m.normalX(), 1e-9);
        assertEquals(0.0, m.normalY(), 1e-9);
        assertEquals(0.0, m.normalZ(), 1e-9);
        assertEquals(0.5, m.penetrationDepth(), 1e-9);
        assertEquals(1, manifold.contacts().size());
        ContactPoint3D cp = manifold.contacts().get(0);
        assertTrue(cp.x() >= 1.5 && cp.x() <= 2.0);
        assertTrue(cp.y() >= 0.5 && cp.y() <= 1.5);
        assertTrue(cp.z() >= 0.5 && cp.z() <= 1.5);
    }

    @Test
    void generatesSphereContact() {
        BoundingSphere a = new BoundingSphere(0, 0, 0, 1.0);
        BoundingSphere b = new BoundingSphere(1.5, 0, 0, 1.0);

        ContactManifold3D manifold = ContactGenerator3D.generate(a, b).orElseThrow();
        assertEquals(1.0, manifold.manifold().normalX(), 1e-9);
        assertEquals(0.5, manifold.manifold().penetrationDepth(), 1e-9);
        assertEquals(1, manifold.contacts().size());
    }

    @Test
    void returnsEmptyWhenNoContact() {
        Aabb a = new Aabb(0, 0, 0, 1, 1, 1);
        Aabb b = new Aabb(2, 2, 2, 3, 3, 3);
        assertTrue(ContactGenerator3D.generate(a, b).isEmpty());
    }
}
