package org.dynamisfx.samples.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import org.dynamisfx.collision.CollisionManifold3D;
import org.dynamisfx.collision.ConvexSupport3D;
import org.dynamisfx.collision.Gjk3D;
import org.dynamisfx.geometry.Vector3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Educational GJK/EPA visualizer with step/run support.
 */
public class GjkEpaVisualizerDemo extends ShapeBaseSample<Group> {

    private static final Vector3D[] SAMPLE_DIRECTIONS = new Vector3D[] {
            new Vector3D(1, 0, 0), new Vector3D(-1, 0, 0), new Vector3D(0, 1, 0), new Vector3D(0, -1, 0),
            new Vector3D(0, 0, 1), new Vector3D(0, 0, -1), new Vector3D(1, 1, 0), new Vector3D(-1, 1, 0),
            new Vector3D(1, -1, 0), new Vector3D(-1, -1, 0), new Vector3D(1, 0, 1), new Vector3D(-1, 0, 1),
            new Vector3D(1, 0, -1), new Vector3D(-1, 0, -1), new Vector3D(0, 1, 1), new Vector3D(0, -1, 1)
    };

    private final Group world = new Group();
    private final Group minkowskiLayer = new Group();
    private final Group simplexLayer = new Group();
    private final Group epaLayer = new Group();

    private final ComboBox<ShapeType> fixedShapeBox = new ComboBox<>();
    private final ComboBox<ShapeType> movingShapeBox = new ComboBox<>();
    private final Slider moveX = new Slider(-220.0, 220.0, 120.0);
    private final Slider moveY = new Slider(-220.0, 220.0, 0.0);
    private final Slider moveZ = new Slider(-220.0, 220.0, 0.0);
    private final Slider stepSlider = new Slider(0.0, SAMPLE_DIRECTIONS.length - 1, 0.0);
    private final Slider speedSlider = new Slider(0.2, 6.0, 1.5);
    private final CheckBox showMinkowski = new CheckBox("Show Minkowski sample cloud");
    private final CheckBox showSimplex = new CheckBox("Show simplex evolution");
    private final CheckBox showEpa = new CheckBox("Show EPA penetration vector");
    private final Label details = new Label();

    private ShapeType fixedType = ShapeType.CUBE;
    private ShapeType movingType = ShapeType.TETRAHEDRON;
    private Node fixedNode;
    private Node movingNode;
    private AnimationTimer runTimer;
    private boolean running;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1400);
        world.getChildren().clear();
        fixedNode = fixedType.createVisual(Color.CORNFLOWERBLUE);
        movingNode = movingType.createVisual(Color.DARKORANGE);
        world.getChildren().addAll(fixedNode, movingNode, minkowskiLayer, simplexLayer, epaLayer);
        model = world;
    }

    @Override
    protected void addMeshAndListeners() {
        refreshAll();
        runTimer = new AnimationTimer() {
            private long last;

            @Override
            public void handle(long now) {
                if (!running) {
                    return;
                }
                if (last == 0L) {
                    last = now;
                    return;
                }
                double dt = (now - last) * 1e-9;
                last = now;
                double next = stepSlider.getValue() + dt * speedSlider.getValue();
                if (next > stepSlider.getMax()) {
                    next = 0.0;
                }
                stepSlider.setValue(next);
            }
        };
        runTimer.start();
        model.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && runTimer != null) {
                runTimer.stop();
            }
        });
    }

    @Override
    protected Node buildControlPanel() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(8));
        root.getChildren().add(new Label("GJK + EPA Visualizer"));

        fixedShapeBox.getItems().setAll(ShapeType.values());
        movingShapeBox.getItems().setAll(ShapeType.values());
        fixedShapeBox.setValue(fixedType);
        movingShapeBox.setValue(movingType);

        fixedShapeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                fixedType = newValue;
                rebuildShapes();
            }
        });
        movingShapeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                movingType = newValue;
                rebuildShapes();
            }
        });

        moveX.valueProperty().addListener((obs, oldValue, newValue) -> refreshAll());
        moveY.valueProperty().addListener((obs, oldValue, newValue) -> refreshAll());
        moveZ.valueProperty().addListener((obs, oldValue, newValue) -> refreshAll());
        stepSlider.valueProperty().addListener((obs, oldValue, newValue) -> refreshAll());

        showMinkowski.setSelected(true);
        showSimplex.setSelected(true);
        showEpa.setSelected(true);
        showMinkowski.selectedProperty().addListener((obs, oldValue, newValue) -> refreshAll());
        showSimplex.selectedProperty().addListener((obs, oldValue, newValue) -> refreshAll());
        showEpa.selectedProperty().addListener((obs, oldValue, newValue) -> refreshAll());

        Button stepForward = new Button("Step +");
        stepForward.setOnAction(event -> stepSlider.setValue(Math.min(stepSlider.getMax(), stepSlider.getValue() + 1)));
        Button stepBack = new Button("Step -");
        stepBack.setOnAction(event -> stepSlider.setValue(Math.max(0.0, stepSlider.getValue() - 1)));
        Button runToggle = new Button("Run / Pause");
        runToggle.setOnAction(event -> running = !running);

        root.getChildren().addAll(
                new Label("Fixed shape"), fixedShapeBox,
                new Label("Moving shape"), movingShapeBox,
                new Label("Move X"), moveX,
                new Label("Move Y"), moveY,
                new Label("Move Z"), moveZ,
                showMinkowski,
                showSimplex,
                showEpa,
                new Label("Iteration step"), stepSlider,
                stepBack,
                stepForward,
                new Label("Run speed"), speedSlider,
                runToggle,
                details);
        return root;
    }

    private void rebuildShapes() {
        world.getChildren().removeAll(fixedNode, movingNode);
        fixedNode = fixedType.createVisual(Color.CORNFLOWERBLUE);
        movingNode = movingType.createVisual(Color.DARKORANGE);
        world.getChildren().add(0, movingNode);
        world.getChildren().add(0, fixedNode);
        refreshAll();
    }

    private void refreshAll() {
        if (movingNode != null) {
            movingNode.setTranslateX(moveX.getValue());
            movingNode.setTranslateY(moveY.getValue());
            movingNode.setTranslateZ(moveZ.getValue());
        }

        ConvexSupport3D fixed = fixedType.supportAt(Vector3D.ZERO);
        ConvexSupport3D moving = movingType.supportAt(new Vector3D(moveX.getValue(), moveY.getValue(), moveZ.getValue()));

        drawMinkowski(fixed, moving);
        drawSimplex(fixed, moving);
        drawEpa(fixed, moving);
    }

    private void drawMinkowski(ConvexSupport3D fixed, ConvexSupport3D moving) {
        minkowskiLayer.getChildren().clear();
        if (!showMinkowski.isSelected()) {
            return;
        }
        for (Vector3D direction : SAMPLE_DIRECTIONS) {
            Vector3D pa = fixed.support(direction);
            Vector3D pb = moving.support(new Vector3D(-direction.x, -direction.y, -direction.z));
            Vector3D md = pa.sub(pb);
            Sphere point = new Sphere(4.0);
            point.setTranslateX(md.x * 0.4);
            point.setTranslateY(md.y * 0.4);
            point.setTranslateZ(md.z * 0.4);
            point.setMaterial(new PhongMaterial(Color.color(0.6, 0.9, 1.0, 0.55)));
            point.setCullFace(CullFace.NONE);
            minkowskiLayer.getChildren().add(point);
        }
    }

    private void drawSimplex(ConvexSupport3D fixed, ConvexSupport3D moving) {
        simplexLayer.getChildren().clear();
        if (!showSimplex.isSelected()) {
            return;
        }
        int count = (int) Math.round(stepSlider.getValue()) + 1;
        List<Vector3D> simplex = new ArrayList<>();
        for (int i = 0; i < Math.min(count, SAMPLE_DIRECTIONS.length); i++) {
            Vector3D d = SAMPLE_DIRECTIONS[i];
            Vector3D pa = fixed.support(d);
            Vector3D pb = moving.support(new Vector3D(-d.x, -d.y, -d.z));
            simplex.add(pa.sub(pb));
        }

        for (int i = 0; i < simplex.size(); i++) {
            Vector3D p = simplex.get(i);
            Sphere s = new Sphere(5.0);
            s.setTranslateX(p.x * 0.55);
            s.setTranslateY(p.y * 0.55);
            s.setTranslateZ(p.z * 0.55);
            double t = (double) i / Math.max(1.0, simplex.size() - 1.0);
            s.setMaterial(new PhongMaterial(Color.color(1.0 - t, t, 0.8)));
            simplexLayer.getChildren().add(s);
        }

        if (!simplex.isEmpty()) {
            Vector3D d = SAMPLE_DIRECTIONS[Math.min(simplex.size() - 1, SAMPLE_DIRECTIONS.length - 1)];
            Vector3D pa = fixed.support(d);
            Vector3D pb = moving.support(new Vector3D(-d.x, -d.y, -d.z));
            details.setText("step=" + simplex.size()
                    + " supportA=(" + r(pa.x) + "," + r(pa.y) + "," + r(pa.z) + ")"
                    + " supportB=(" + r(pb.x) + "," + r(pb.y) + "," + r(pb.z) + ")"
                    + " dot=" + r(pa.dotProduct(d) - pb.dotProduct(d)));
        }
    }

    private void drawEpa(ConvexSupport3D fixed, ConvexSupport3D moving) {
        epaLayer.getChildren().clear();
        Optional<CollisionManifold3D> manifold = Gjk3D.intersectsWithManifold(fixed, moving);
        if (manifold.isEmpty()) {
            return;
        }
        CollisionManifold3D m = manifold.get();
        if (!showEpa.isSelected()) {
            details.setText(details.getText() + "  |  penetrating depth=" + r(m.penetrationDepth()));
            return;
        }

        Vector3D n = new Vector3D(m.normalX(), m.normalY(), m.normalZ());
        double len = Math.max(10.0, m.penetrationDepth() * 120.0);
        int markers = 8;
        for (int i = 0; i <= markers; i++) {
            double t = i / (double) markers;
            Sphere marker = new Sphere(3.2);
            marker.setTranslateX(n.x * len * t);
            marker.setTranslateY(n.y * len * t);
            marker.setTranslateZ(n.z * len * t);
            marker.setMaterial(new PhongMaterial(Color.LIMEGREEN));
            epaLayer.getChildren().add(marker);
        }
        details.setText(details.getText() + "  |  EPA normal=(" + r(n.x) + "," + r(n.y) + "," + r(n.z)
                + ") depth=" + r(m.penetrationDepth()));
    }

    private static String r(double value) {
        return String.valueOf(Math.round(value * 1000.0) / 1000.0);
    }

    private enum ShapeType {
        TETRAHEDRON,
        CUBE,
        DODECAHEDRON,
        CYLINDER,
        CAPSULE;

        private Node createVisual(Color color) {
            return switch (this) {
                case CUBE -> {
                    Box box = new Box(120.0, 120.0, 120.0);
                    box.setMaterial(new PhongMaterial(color));
                    box.setCullFace(CullFace.NONE);
                    yield box;
                }
                case CYLINDER -> {
                    Cylinder c = new Cylinder(48.0, 140.0);
                    c.setMaterial(new PhongMaterial(color));
                    c.setCullFace(CullFace.NONE);
                    yield c;
                }
                case CAPSULE -> {
                    Sphere s = new Sphere(62.0);
                    s.setMaterial(new PhongMaterial(color));
                    s.setCullFace(CullFace.NONE);
                    yield s;
                }
                case DODECAHEDRON -> {
                    Sphere s = new Sphere(70.0);
                    s.setMaterial(new PhongMaterial(color));
                    s.setCullFace(CullFace.NONE);
                    s.setDrawMode(DrawMode.LINE);
                    yield s;
                }
                case TETRAHEDRON -> {
                    Box b = new Box(140.0, 120.0, 100.0);
                    b.setMaterial(new PhongMaterial(color));
                    b.setCullFace(CullFace.NONE);
                    b.setRotate(35.0);
                    yield b;
                }
            };
        }

        private ConvexSupport3D supportAt(Vector3D center) {
            return switch (this) {
                case CUBE -> direction -> new Vector3D(
                        center.x + Math.signum(direction.x) * 60.0,
                        center.y + Math.signum(direction.y) * 60.0,
                        center.z + Math.signum(direction.z) * 60.0);
                case CYLINDER -> direction -> {
                    double y = center.y + Math.signum(direction.y) * 70.0;
                    double len = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
                    double x = center.x + (len < 1e-8 ? 0.0 : direction.x / len * 48.0);
                    double z = center.z + (len < 1e-8 ? 0.0 : direction.z / len * 48.0);
                    return new Vector3D(x, y, z);
                };
                case CAPSULE -> direction -> {
                    Vector3D d = new Vector3D(direction);
                    d.normalize();
                    Vector3D spine = center.add(0.0, Math.signum(direction.y) * 40.0, 0.0);
                    return spine.add(d.x * 42.0, d.y * 42.0, d.z * 42.0);
                };
                case DODECAHEDRON -> direction -> {
                    double phi = (1.0 + Math.sqrt(5.0)) * 0.5;
                    double scale = 40.0;
                    Vector3D[] verts = new Vector3D[] {
                            new Vector3D(-1, -1, -1), new Vector3D(1, -1, -1), new Vector3D(-1, 1, -1), new Vector3D(1, 1, -1),
                            new Vector3D(-1, -1, 1), new Vector3D(1, -1, 1), new Vector3D(-1, 1, 1), new Vector3D(1, 1, 1),
                            new Vector3D(0, -1 / phi, -phi), new Vector3D(0, 1 / phi, -phi),
                            new Vector3D(0, -1 / phi, phi), new Vector3D(0, 1 / phi, phi),
                            new Vector3D(-1 / phi, -phi, 0), new Vector3D(1 / phi, -phi, 0),
                            new Vector3D(-1 / phi, phi, 0), new Vector3D(1 / phi, phi, 0),
                            new Vector3D(-phi, 0, -1 / phi), new Vector3D(phi, 0, -1 / phi),
                            new Vector3D(-phi, 0, 1 / phi), new Vector3D(phi, 0, 1 / phi)
                    };
                    Vector3D best = verts[0];
                    double bestDot = best.dotProduct(direction);
                    for (int i = 1; i < verts.length; i++) {
                        double dDot = verts[i].dotProduct(direction);
                        if (dDot > bestDot) {
                            bestDot = dDot;
                            best = verts[i];
                        }
                    }
                    return center.add(best.x * scale, best.y * scale, best.z * scale);
                };
                case TETRAHEDRON -> direction -> {
                    Vector3D[] verts = new Vector3D[] {
                            new Vector3D(0, 70, 0),
                            new Vector3D(-65, -45, 55),
                            new Vector3D(65, -45, 55),
                            new Vector3D(0, -45, -70)
                    };
                    Vector3D best = verts[0];
                    double bestDot = best.dotProduct(direction);
                    for (int i = 1; i < verts.length; i++) {
                        double dot = verts[i].dotProduct(direction);
                        if (dot > bestDot) {
                            bestDot = dot;
                            best = verts[i];
                        }
                    }
                    return center.add(best.x, best.y, best.z);
                };
            };
        }
    }
}
