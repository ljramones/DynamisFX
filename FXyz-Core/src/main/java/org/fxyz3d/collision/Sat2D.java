package org.fxyz3d.collision;

import java.util.List;
import java.util.Optional;
import javafx.geometry.Point2D;

/**
 * Separating Axis Theorem checks for 2D convex polygons.
 */
public final class Sat2D {

    private static final double EPSILON = 1e-9;

    private Sat2D() {
    }

    public static boolean intersects(ConvexPolygon2D a, ConvexPolygon2D b) {
        return intersectsWithManifold(a, b).isPresent();
    }

    public static Optional<CollisionManifold2D> intersectsWithManifold(ConvexPolygon2D a, ConvexPolygon2D b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("polygons must not be null");
        }

        Point2D centerA = a.centroid();
        Point2D centerB = b.centroid();
        Point2D centerDelta = centerB.subtract(centerA);

        double minOverlap = Double.POSITIVE_INFINITY;
        double bestAxisX = 0.0;
        double bestAxisY = 0.0;

        AxisResult fromA = evaluateAxes(a.vertices(), a, b, centerDelta, minOverlap, bestAxisX, bestAxisY);
        if (!fromA.hit()) {
            return Optional.empty();
        }
        minOverlap = fromA.minOverlap();
        bestAxisX = fromA.axisX();
        bestAxisY = fromA.axisY();

        AxisResult fromB = evaluateAxes(b.vertices(), a, b, centerDelta, minOverlap, bestAxisX, bestAxisY);
        if (!fromB.hit()) {
            return Optional.empty();
        }
        minOverlap = fromB.minOverlap();
        bestAxisX = fromB.axisX();
        bestAxisY = fromB.axisY();

        return Optional.of(new CollisionManifold2D(bestAxisX, bestAxisY, minOverlap));
    }

    public static ProjectionInterval project(ConvexPolygon2D polygon, double axisX, double axisY) {
        if (polygon == null) {
            throw new IllegalArgumentException("polygon must not be null");
        }
        if (!Double.isFinite(axisX) || !Double.isFinite(axisY)) {
            throw new IllegalArgumentException("axis components must be finite");
        }
        double axisLengthSq = axisX * axisX + axisY * axisY;
        if (axisLengthSq <= EPSILON) {
            throw new IllegalArgumentException("axis must be non-zero");
        }
        double invLen = 1.0 / Math.sqrt(axisLengthSq);
        double nx = axisX * invLen;
        double ny = axisY * invLen;

        List<Point2D> points = polygon.vertices();
        double first = points.get(0).getX() * nx + points.get(0).getY() * ny;
        double min = first;
        double max = first;
        for (int i = 1; i < points.size(); i++) {
            Point2D p = points.get(i);
            double projection = p.getX() * nx + p.getY() * ny;
            min = Math.min(min, projection);
            max = Math.max(max, projection);
        }
        return new ProjectionInterval(min, max);
    }

    private static AxisResult evaluateAxes(
            List<Point2D> sourceVertices,
            ConvexPolygon2D a,
            ConvexPolygon2D b,
            Point2D centerDelta,
            double startMinOverlap,
            double startAxisX,
            double startAxisY) {

        double minOverlap = startMinOverlap;
        double bestAxisX = startAxisX;
        double bestAxisY = startAxisY;

        for (int i = 0; i < sourceVertices.size(); i++) {
            Point2D p0 = sourceVertices.get(i);
            Point2D p1 = sourceVertices.get((i + 1) % sourceVertices.size());
            double edgeX = p1.getX() - p0.getX();
            double edgeY = p1.getY() - p0.getY();
            double axisX = -edgeY;
            double axisY = edgeX;
            double axisLengthSq = axisX * axisX + axisY * axisY;
            if (axisLengthSq <= EPSILON) {
                continue;
            }
            double invLen = 1.0 / Math.sqrt(axisLengthSq);
            axisX *= invLen;
            axisY *= invLen;

            ProjectionInterval projA = project(a, axisX, axisY);
            ProjectionInterval projB = project(b, axisX, axisY);
            double overlap = projA.overlapDepth(projB);
            if (overlap < 0.0) {
                return AxisResult.miss();
            }
            if (overlap < minOverlap) {
                double orientedAxisX = axisX;
                double orientedAxisY = axisY;
                if ((centerDelta.getX() * axisX + centerDelta.getY() * axisY) < 0.0) {
                    orientedAxisX = -axisX;
                    orientedAxisY = -axisY;
                }
                minOverlap = overlap;
                bestAxisX = orientedAxisX;
                bestAxisY = orientedAxisY;
            }
        }
        return new AxisResult(true, minOverlap, bestAxisX, bestAxisY);
    }

    private record AxisResult(boolean hit, double minOverlap, double axisX, double axisY) {
        private static AxisResult miss() {
            return new AxisResult(false, 0.0, 0.0, 0.0);
        }
    }
}
