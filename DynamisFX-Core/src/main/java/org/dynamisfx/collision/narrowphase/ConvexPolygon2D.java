package org.dynamisfx.collision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Point2D;

/**
 * Immutable convex polygon in 2D.
 */
public final class ConvexPolygon2D {

    private static final double EPSILON = 1e-9;

    private final List<Point2D> vertices;

    public ConvexPolygon2D(List<Point2D> vertices) {
        if (vertices == null) {
            throw new IllegalArgumentException("vertices must not be null");
        }
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("convex polygon requires at least 3 points");
        }
        List<Point2D> copy = new ArrayList<>(vertices.size());
        for (int i = 0; i < vertices.size(); i++) {
            Point2D p = vertices.get(i);
            if (p == null || !Double.isFinite(p.getX()) || !Double.isFinite(p.getY())) {
                throw new IllegalArgumentException("all vertices must be non-null finite points");
            }
            copy.add(p);
        }
        validateEdges(copy);
        validateConvex(copy);
        this.vertices = List.copyOf(copy);
    }

    public static ConvexPolygon2D of(double... coordinates) {
        if (coordinates == null || coordinates.length < 6 || coordinates.length % 2 != 0) {
            throw new IllegalArgumentException("coordinates must contain at least 3 xy pairs");
        }
        List<Point2D> points = new ArrayList<>(coordinates.length / 2);
        for (int i = 0; i < coordinates.length; i += 2) {
            points.add(new Point2D(coordinates[i], coordinates[i + 1]));
        }
        return new ConvexPolygon2D(points);
    }

    public List<Point2D> vertices() {
        return Collections.unmodifiableList(vertices);
    }

    public int vertexCount() {
        return vertices.size();
    }

    public Point2D centroid() {
        double sumX = 0.0;
        double sumY = 0.0;
        for (Point2D p : vertices) {
            sumX += p.getX();
            sumY += p.getY();
        }
        return new Point2D(sumX / vertices.size(), sumY / vertices.size());
    }

    private static void validateEdges(List<Point2D> points) {
        for (int i = 0; i < points.size(); i++) {
            Point2D a = points.get(i);
            Point2D b = points.get((i + 1) % points.size());
            if (a.distance(b) <= EPSILON) {
                throw new IllegalArgumentException("polygon contains duplicate consecutive points");
            }
        }
    }

    private static void validateConvex(List<Point2D> points) {
        int sign = 0;
        for (int i = 0; i < points.size(); i++) {
            Point2D a = points.get(i);
            Point2D b = points.get((i + 1) % points.size());
            Point2D c = points.get((i + 2) % points.size());
            double cross = crossZ(a, b, c);
            if (Math.abs(cross) <= EPSILON) {
                continue;
            }
            int currentSign = cross > 0 ? 1 : -1;
            if (sign == 0) {
                sign = currentSign;
            } else if (sign != currentSign) {
                throw new IllegalArgumentException("polygon must be convex and ordered");
            }
        }
        if (sign == 0) {
            throw new IllegalArgumentException("polygon points are collinear");
        }
    }

    private static double crossZ(Point2D a, Point2D b, Point2D c) {
        double abX = b.getX() - a.getX();
        double abY = b.getY() - a.getY();
        double bcX = c.getX() - b.getX();
        double bcY = c.getY() - b.getY();
        return abX * bcY - abY * bcX;
    }
}
