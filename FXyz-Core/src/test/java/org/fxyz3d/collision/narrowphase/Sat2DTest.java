package org.fxyz3d.collision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

class Sat2DTest {

    @Test
    void constructorRejectsConcavePolygon() {
        List<Point2D> concave = List.of(
                new Point2D(0, 0),
                new Point2D(2, 0),
                new Point2D(1, 1),
                new Point2D(2, 2),
                new Point2D(0, 2));

        assertThrows(IllegalArgumentException.class, () -> new ConvexPolygon2D(concave));
    }

    @Test
    void projectionIntervalDetectsOverlap() {
        ProjectionInterval a = new ProjectionInterval(0.0, 2.0);
        ProjectionInterval b = new ProjectionInterval(2.0, 5.0);
        ProjectionInterval c = new ProjectionInterval(2.1, 3.0);

        assertTrue(a.overlaps(b));
        assertTrue(a.overlapDepth(b) >= 0.0);
        assertFalse(a.overlaps(c));
        assertTrue(a.overlapDepth(c) < 0.0);
    }

    @Test
    void satDetectsSeparatedAndOverlappingPolygons() {
        ConvexPolygon2D a = rectangle(0, 0, 2, 2, 0);
        ConvexPolygon2D b = rectangle(5, 0, 2, 2, 0);
        ConvexPolygon2D c = rectangle(1.5, 0, 2, 2, Math.toRadians(30));

        assertFalse(Sat2D.intersects(a, b));
        assertTrue(Sat2D.intersects(a, c));
    }

    @Test
    void satTreatsTouchingEdgesAsIntersection() {
        ConvexPolygon2D a = rectangle(0, 0, 2, 2, 0);
        ConvexPolygon2D b = rectangle(2, 0, 2, 2, 0);

        assertTrue(Sat2D.intersects(a, b));
        Optional<CollisionManifold2D> manifold = Sat2D.intersectsWithManifold(a, b);
        assertTrue(manifold.isPresent());
        assertEquals(0.0, manifold.get().penetrationDepth(), 1e-9);
    }

    @Test
    void manifoldUsesMinimumTranslationAxis() {
        ConvexPolygon2D a = rectangle(0, 0, 2, 2, 0);
        ConvexPolygon2D b = rectangle(1.2, 0.1, 2, 2, Math.toRadians(15));

        CollisionManifold2D manifold = Sat2D.intersectsWithManifold(a, b).orElseThrow();
        assertTrue(manifold.penetrationDepth() > 0.0);

        double len = Math.hypot(manifold.normalX(), manifold.normalY());
        assertEquals(1.0, len, 1e-6);

        double centerDeltaX = b.centroid().getX() - a.centroid().getX();
        double centerDeltaY = b.centroid().getY() - a.centroid().getY();
        double directionalDot = centerDeltaX * manifold.normalX() + centerDeltaY * manifold.normalY();
        assertTrue(directionalDot >= -1e-9);
    }

    private static ConvexPolygon2D rectangle(
            double centerX,
            double centerY,
            double width,
            double height,
            double angleRadians) {
        double hw = width * 0.5;
        double hh = height * 0.5;
        Point2D[] local = new Point2D[] {
                new Point2D(-hw, -hh),
                new Point2D(hw, -hh),
                new Point2D(hw, hh),
                new Point2D(-hw, hh)
        };
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);
        List<Point2D> world = java.util.Arrays.stream(local)
                .map(p -> new Point2D(
                        centerX + (p.getX() * cos - p.getY() * sin),
                        centerY + (p.getX() * sin + p.getY() * cos)))
                .toList();
        return new ConvexPolygon2D(world);
    }
}
