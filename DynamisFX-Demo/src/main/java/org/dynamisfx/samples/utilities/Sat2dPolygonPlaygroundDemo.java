package org.dynamisfx.samples.utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import org.dynamisfx.collision.CollisionManifold2D;
import org.dynamisfx.collision.ConvexPolygon2D;
import org.dynamisfx.collision.ProjectionInterval;
import org.dynamisfx.collision.Sat2D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * SAT2D playground with draggable polygons and axis/MTV visualization.
 */
public class Sat2dPolygonPlaygroundDemo extends ShapeBaseSample<Group> {

    private final Group world = new Group();
    private final Group overlay = new Group();
    private final List<PolyHandle> polys = new ArrayList<>();
    private final List<Point2D> pendingPoints = new ArrayList<>();

    private final Label status = new Label();
    private final CheckBox showAxes = new CheckBox("Show tested axes");
    private final CheckBox createMode = new CheckBox("Create polygon mode");

    private AnimationTimer timer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-900);
        world.getChildren().clear();
        overlay.getChildren().clear();
        polys.clear();

        PolyHandle a = makePoly(Color.CORNFLOWERBLUE, List.of(
                new Point2D(-90, -60), new Point2D(-10, -75), new Point2D(70, -10), new Point2D(20, 50), new Point2D(-80, 30)));
        PolyHandle b = makePoly(Color.DARKORANGE, List.of(
                new Point2D(120, -40), new Point2D(200, -70), new Point2D(250, 0), new Point2D(210, 80), new Point2D(110, 60)));
        polys.add(a);
        polys.add(b);

        world.getChildren().addAll(a.polygon, b.polygon, overlay);
        model = world;
    }

    @Override
    protected void addMeshAndListeners() {
        for (PolyHandle poly : polys) {
            installDrag(poly);
        }

        subScene.setOnMouseClicked(event -> {
            if (!createMode.isSelected()) {
                return;
            }
            Point2D local = new Point2D(event.getX() - subScene.getWidth() * 0.5, event.getY() - subScene.getHeight() * 0.5);
            if (event.isSecondaryButtonDown()) {
                finalizePendingPolygon();
                return;
            }
            pendingPoints.add(local);
            status.setText("Create mode points=" + pendingPoints.size() + " (right-click to finalize)");
        });

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateOverlay();
            }
        };
        timer.start();
        model.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && timer != null) {
                timer.stop();
            }
        });
    }

    @Override
    protected Node buildControlPanel() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(8));
        root.getChildren().add(new Label("SAT2D Polygon Playground"));
        showAxes.setSelected(true);
        root.getChildren().addAll(showAxes, createMode);

        Button addRandom = new Button("Add random convex polygon");
        addRandom.setOnAction(event -> addRandomPolygon());
        root.getChildren().addAll(addRandom, status);
        return root;
    }

    private void updateOverlay() {
        overlay.getChildren().clear();
        if (polys.size() < 2) {
            return;
        }

        PolyHandle a = polys.get(0);
        PolyHandle b = polys.get(1);
        ConvexPolygon2D left = toConvex(a);
        ConvexPolygon2D right = toConvex(b);

        Optional<CollisionManifold2D> manifold = Sat2D.intersectsWithManifold(left, right);
        if (manifold.isPresent()) {
            CollisionManifold2D mtv = manifold.get();
            Point2D c = left.centroid();
            Point2D end = new Point2D(
                    c.getX() + mtv.normalX() * mtv.penetrationDepth() * 50.0,
                    c.getY() + mtv.normalY() * mtv.penetrationDepth() * 50.0);
            Line mtvLine = line(c, end, Color.LIMEGREEN, 3.0);
            overlay.getChildren().add(mtvLine);
            status.setText("Collision: MTV depth=" + round(mtv.penetrationDepth()));
        } else {
            status.setText("No collision: separating axis found");
        }

        if (showAxes.isSelected()) {
            drawAxes(left, right);
        }
    }

    private void drawAxes(ConvexPolygon2D left, ConvexPolygon2D right) {
        List<Point2D> axes = new ArrayList<>();
        collectAxes(left.vertices(), axes);
        collectAxes(right.vertices(), axes);
        for (Point2D axis : axes) {
            ProjectionInterval a = Sat2D.project(left, axis.getX(), axis.getY());
            ProjectionInterval b = Sat2D.project(right, axis.getX(), axis.getY());
            boolean overlap = a.overlapDepth(b) >= 0.0;
            Point2D center = left.centroid();
            Point2D dir = axis.normalize();
            Point2D p0 = center.subtract(dir.multiply(500.0));
            Point2D p1 = center.add(dir.multiply(500.0));
            Line axisLine = line(p0, p1, overlap ? Color.RED : Color.LIGHTGREEN, 1.2);
            axisLine.getStrokeDashArray().setAll(8.0, 6.0);
            overlay.getChildren().add(axisLine);
            if (!overlap) {
                break;
            }
        }
    }

    private void collectAxes(List<Point2D> vertices, List<Point2D> axes) {
        for (int i = 0; i < vertices.size(); i++) {
            Point2D a = vertices.get(i);
            Point2D b = vertices.get((i + 1) % vertices.size());
            Point2D edge = b.subtract(a);
            Point2D axis = new Point2D(-edge.getY(), edge.getX());
            if (axis.magnitude() > 1e-6) {
                axes.add(axis);
            }
        }
    }

    private ConvexPolygon2D toConvex(PolyHandle handle) {
        List<Point2D> points = new ArrayList<>();
        for (int i = 0; i < handle.baseVertices.size(); i++) {
            Point2D p = handle.baseVertices.get(i);
            points.add(p.add(handle.translateX, handle.translateY));
        }
        return new ConvexPolygon2D(points);
    }

    private PolyHandle makePoly(Color color, List<Point2D> points) {
        Polygon polygon = new Polygon();
        polygon.setStroke(Color.WHITE);
        polygon.setFill(color.deriveColor(0.0, 1.0, 1.0, 0.35));
        polygon.setStrokeWidth(2.0);
        for (Point2D p : points) {
            polygon.getPoints().addAll(p.getX(), p.getY());
        }
        return new PolyHandle(polygon, new ArrayList<>(points));
    }

    private void installDrag(PolyHandle poly) {
        final double[] last = new double[2];
        poly.polygon.setOnMousePressed(event -> {
            last[0] = event.getSceneX();
            last[1] = event.getSceneY();
            event.consume();
        });
        poly.polygon.setOnMouseDragged(event -> {
            double dx = event.getSceneX() - last[0];
            double dy = event.getSceneY() - last[1];
            last[0] = event.getSceneX();
            last[1] = event.getSceneY();
            poly.translateX += dx;
            poly.translateY += dy;
            poly.polygon.setTranslateX(poly.translateX);
            poly.polygon.setTranslateY(poly.translateY);
            event.consume();
        });
    }

    private void finalizePendingPolygon() {
        if (pendingPoints.size() < 3) {
            pendingPoints.clear();
            return;
        }
        List<Point2D> hull = convexHull(pendingPoints);
        pendingPoints.clear();
        if (hull.size() < 3) {
            return;
        }
        PolyHandle handle = makePoly(Color.MEDIUMPURPLE, hull);
        polys.add(handle);
        installDrag(handle);
        world.getChildren().add(handle.polygon);
        status.setText("Created polygon vertices=" + hull.size());
    }

    private void addRandomPolygon() {
        List<Point2D> points = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            points.add(new Point2D(Math.random() * 320.0 - 160.0, Math.random() * 220.0 - 110.0));
        }
        List<Point2D> hull = convexHull(points);
        if (hull.size() < 3) {
            return;
        }
        PolyHandle handle = makePoly(Color.CADETBLUE, hull);
        polys.add(handle);
        installDrag(handle);
        world.getChildren().add(handle.polygon);
    }

    private static List<Point2D> convexHull(List<Point2D> points) {
        List<Point2D> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingDouble(Point2D::getX).thenComparingDouble(Point2D::getY));
        if (sorted.size() < 3) {
            return sorted;
        }
        List<Point2D> lower = new ArrayList<>();
        for (Point2D p : sorted) {
            while (lower.size() >= 2 && cross(lower.get(lower.size() - 2), lower.get(lower.size() - 1), p) <= 0.0) {
                lower.remove(lower.size() - 1);
            }
            lower.add(p);
        }
        List<Point2D> upper = new ArrayList<>();
        for (int i = sorted.size() - 1; i >= 0; i--) {
            Point2D p = sorted.get(i);
            while (upper.size() >= 2 && cross(upper.get(upper.size() - 2), upper.get(upper.size() - 1), p) <= 0.0) {
                upper.remove(upper.size() - 1);
            }
            upper.add(p);
        }
        lower.remove(lower.size() - 1);
        upper.remove(upper.size() - 1);
        lower.addAll(upper);
        return lower;
    }

    private static double cross(Point2D a, Point2D b, Point2D c) {
        return (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
    }

    private static Line line(Point2D from, Point2D to, Color color, double width) {
        Line line = new Line(from.getX(), from.getY(), to.getX(), to.getY());
        line.setStroke(color);
        line.setStrokeWidth(width);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        return line;
    }

    private static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private static final class PolyHandle {
        private final Polygon polygon;
        private final List<Point2D> baseVertices;
        private double translateX;
        private double translateY;

        private PolyHandle(Polygon polygon, List<Point2D> baseVertices) {
            this.polygon = polygon;
            this.baseVertices = baseVertices;
        }
    }
}
