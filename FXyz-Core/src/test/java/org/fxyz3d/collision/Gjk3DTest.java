package org.fxyz3d.collision;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.fxyz3d.geometry.Vector3D;
import org.junit.jupiter.api.Test;

class Gjk3DTest {

    @Test
    void detectsIntersectingAabbs() {
        ConvexSupport3D a = Gjk3D.fromAabb(new Aabb(0, 0, 0, 2, 2, 2));
        ConvexSupport3D b = Gjk3D.fromAabb(new Aabb(1.5, 1.5, 1.5, 3, 3, 3));
        ConvexSupport3D c = Gjk3D.fromAabb(new Aabb(4, 4, 4, 5, 5, 5));

        assertTrue(Gjk3D.intersects(a, b));
        assertFalse(Gjk3D.intersects(a, c));
    }

    @Test
    void detectsSphereAndAabbIntersection() {
        ConvexSupport3D sphere = Gjk3D.fromBoundingSphere(new BoundingSphere(0, 0, 0, 1));
        ConvexSupport3D nearBox = Gjk3D.fromAabb(new Aabb(0.5, -0.25, -0.25, 2, 0.25, 0.25));
        ConvexSupport3D farBox = Gjk3D.fromAabb(new Aabb(3, 3, 3, 4, 4, 4));

        assertTrue(Gjk3D.intersects(sphere, nearBox));
        assertFalse(Gjk3D.intersects(sphere, farBox));
    }

    @Test
    void worksWithCustomConvexHullSupport() {
        ConvexSupport3D tetraA = pointCloudSupport(List.of(
                new Vector3D(0, 0, 0),
                new Vector3D(1, 0, 0),
                new Vector3D(0, 1, 0),
                new Vector3D(0, 0, 1)));

        ConvexSupport3D tetraB = pointCloudSupport(List.of(
                new Vector3D(0.3, 0.3, 0.3),
                new Vector3D(1.3, 0.3, 0.3),
                new Vector3D(0.3, 1.3, 0.3),
                new Vector3D(0.3, 0.3, 1.3)));

        ConvexSupport3D tetraC = pointCloudSupport(List.of(
                new Vector3D(5, 5, 5),
                new Vector3D(6, 5, 5),
                new Vector3D(5, 6, 5),
                new Vector3D(5, 5, 6)));

        assertTrue(Gjk3D.intersects(tetraA, tetraB));
        assertFalse(Gjk3D.intersects(tetraA, tetraC));
    }

    @Test
    void returnsManifoldForIntersectingConvexShapes() {
        ConvexSupport3D a = Gjk3D.fromAabb(new Aabb(0, 0, 0, 2, 2, 2));
        ConvexSupport3D b = Gjk3D.fromAabb(new Aabb(1.8, 0, 0, 3.8, 2, 2));

        CollisionManifold3D manifold = Gjk3D.intersectsWithManifold(a, b).orElseThrow();
        assertNotNull(manifold);
        assertTrue(manifold.penetrationDepth() > 0.0);

        double len = Math.sqrt(
                manifold.normalX() * manifold.normalX()
                        + manifold.normalY() * manifold.normalY()
                        + manifold.normalZ() * manifold.normalZ());
        assertTrue(Math.abs(len - 1.0) < 1e-6);
    }

    @Test
    void manifoldIsEmptyForSeparatedShapes() {
        ConvexSupport3D a = Gjk3D.fromAabb(new Aabb(0, 0, 0, 1, 1, 1));
        ConvexSupport3D b = Gjk3D.fromAabb(new Aabb(10, 10, 10, 11, 11, 11));
        assertTrue(Gjk3D.intersectsWithManifold(a, b).isEmpty());
    }

    private static ConvexSupport3D pointCloudSupport(List<Vector3D> points) {
        return direction -> {
            Vector3D best = points.get(0);
            double bestDot = dot(best, direction);
            for (int i = 1; i < points.size(); i++) {
                Vector3D candidate = points.get(i);
                double score = dot(candidate, direction);
                if (score > bestDot) {
                    bestDot = score;
                    best = candidate;
                }
            }
            return new Vector3D(best.getX(), best.getY(), best.getZ());
        };
    }

    private static double dot(Vector3D a, Vector3D b) {
        return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
    }
}
