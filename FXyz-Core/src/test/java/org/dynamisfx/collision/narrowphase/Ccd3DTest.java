package org.dynamisfx.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalDouble;
import org.dynamisfx.geometry.Vector3D;
import org.junit.jupiter.api.Test;

class Ccd3DTest {

    @Test
    void segmentAabbTimeOfImpactHitsAtExpectedFraction() {
        Aabb box = new Aabb(5, -1, -1, 6, 1, 1);
        Vector3D start = new Vector3D(0, 0, 0);
        Vector3D end = new Vector3D(10, 0, 0);

        OptionalDouble toi = Ccd3D.segmentAabbTimeOfImpact(start, end, box);
        assertTrue(toi.isPresent());
        assertEquals(0.5, toi.getAsDouble(), 1e-9);
    }

    @Test
    void segmentAabbTimeOfImpactReturnsEmptyOnMiss() {
        Aabb box = new Aabb(5, 5, 5, 6, 6, 6);
        Vector3D start = new Vector3D(0, 0, 0);
        Vector3D end = new Vector3D(1, 0, 0);

        assertTrue(Ccd3D.segmentAabbTimeOfImpact(start, end, box).isEmpty());
    }

    @Test
    void sweptAabbTimeOfImpactMatchesExpectedContact() {
        Aabb moving = new Aabb(0, 0, 0, 1, 1, 1);
        Aabb target = new Aabb(5, 0, 0, 6, 1, 1);
        Vector3D delta = new Vector3D(10, 0, 0);

        OptionalDouble toi = Ccd3D.sweptAabbTimeOfImpact(moving, delta, target);
        assertTrue(toi.isPresent());
        assertEquals(0.4, toi.getAsDouble(), 1e-9);
    }

    @Test
    void sweptAabbTimeOfImpactIsZeroWhenStartingOverlapped() {
        Aabb moving = new Aabb(0, 0, 0, 2, 2, 2);
        Aabb target = new Aabb(1, 1, 1, 3, 3, 3);
        Vector3D delta = new Vector3D(5, 0, 0);

        OptionalDouble toi = Ccd3D.sweptAabbTimeOfImpact(moving, delta, target);
        assertTrue(toi.isPresent());
        assertEquals(0.0, toi.getAsDouble(), 1e-9);
    }

    @Test
    void sweptConvexTimeOfImpactDetectsCollision() {
        ConvexSupport3D shapeA = Gjk3D.fromAabb(new Aabb(0, 0, 0, 1, 1, 1));
        ConvexSupport3D shapeB = Gjk3D.fromAabb(new Aabb(3, 0, 0, 4, 1, 1));
        Vector3D deltaA = new Vector3D(4, 0, 0);
        Vector3D deltaB = new Vector3D(0, 0, 0);

        OptionalDouble toi = Ccd3D.sweptConvexTimeOfImpact(shapeA, deltaA, shapeB, deltaB, 64, 24);
        assertTrue(toi.isPresent());
        assertEquals(0.5, toi.getAsDouble(), 0.02);
    }

    @Test
    void sweptConvexTimeOfImpactReturnsEmptyWhenNoHit() {
        ConvexSupport3D shapeA = Gjk3D.fromAabb(new Aabb(0, 0, 0, 1, 1, 1));
        ConvexSupport3D shapeB = Gjk3D.fromAabb(new Aabb(10, 0, 0, 11, 1, 1));

        OptionalDouble toi = Ccd3D.sweptConvexTimeOfImpact(
                shapeA, new Vector3D(1, 0, 0),
                shapeB, new Vector3D(0, 0, 0));
        assertTrue(toi.isEmpty());
    }

    @Test
    void sweptConvexTimeOfImpactIsZeroForInitialOverlap() {
        ConvexSupport3D shapeA = Gjk3D.fromAabb(new Aabb(0, 0, 0, 2, 2, 2));
        ConvexSupport3D shapeB = Gjk3D.fromAabb(new Aabb(1, 1, 1, 3, 3, 3));

        OptionalDouble toi = Ccd3D.sweptConvexTimeOfImpact(
                shapeA, new Vector3D(5, 0, 0),
                shapeB, new Vector3D(0, 0, 0));
        assertTrue(toi.isPresent());
        assertEquals(0.0, toi.getAsDouble(), 1e-9);
    }
}
