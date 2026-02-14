package org.dynamisfx.samples.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import org.dynamisfx.collision.Aabb;
import org.dynamisfx.collision.CollisionFilter;
import org.dynamisfx.collision.CollisionFiltering;
import org.dynamisfx.collision.CollisionPair;
import org.dynamisfx.collision.CollisionWorld3D;
import org.dynamisfx.collision.ContactGenerator3D;
import org.dynamisfx.collision.ContactSolver3D;
import org.dynamisfx.collision.FilteredCollisionPair;
import org.dynamisfx.collision.RigidBodyAdapter3D;
import org.dynamisfx.collision.SweepAndPrune3D;
import org.dynamisfx.geometry.Vector3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Demonstrates collision layer filtering with live pair counters.
 */
public class CollisionFilterLayerDemo extends ShapeBaseSample<Group> {

    private static final Color[] LAYER_COLORS = new Color[] {
            Color.RED, Color.DODGERBLUE, Color.LIMEGREEN, Color.GOLD
    };

    private final Group world = new Group();
    private final SweepAndPrune3D<Body> broadPhase = new SweepAndPrune3D<>();
    private final BodyAdapter adapter = new BodyAdapter();
    private final ContactSolver3D<Body> solver = new ContactSolver3D<>(adapter);
    private final CollisionWorld3D<Body> collisionWorld = new CollisionWorld3D<>(
            broadPhase,
            Body::aabb,
            body -> body.filter,
            (a, b) -> ContactGenerator3D.generate(a.aabb(), b.aabb()));

    private final List<Body> bodies = new ArrayList<>();
    private final int[] masks = new int[] {0xF, 0xF, 0xF, 0xF};

    private Slider countSlider;
    private Label stats = new Label();
    private AnimationTimer timer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1400);
        world.getChildren().clear();
        bodies.clear();

        Body floor = createBody(0, -280, 0, 900, 40, 480, 0, 0);
        floor.dynamic = false;
        floor.node.setMaterial(new PhongMaterial(Color.DIMGRAY));
        bodies.add(floor);
        world.getChildren().add(floor.node);

        collisionWorld.setBodyAdapter(adapter);
        collisionWorld.setResponder(solver);
        collisionWorld.setGravity(new Vector3D(0, -950, 0));
        collisionWorld.setSolverIterations(8);

        spawnBodies(48);
        model = world;
    }

    @Override
    protected void addMeshAndListeners() {
        timer = new AnimationTimer() {
            private long last;

            @Override
            public void handle(long now) {
                if (last == 0L) {
                    last = now;
                    return;
                }
                double dt = Math.min((now - last) * 1e-9, 1.0 / 25.0);
                last = now;
                collisionWorld.step(bodies, dt);
                updateCounters();
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
        root.getChildren().add(new Label("Collision Filter Layer Demo"));

        countSlider = slider(12, 120, 48);
        countSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            spawnBodies(newValue.intValue());
        });

        GridPane matrix = new GridPane();
        matrix.setHgap(6);
        matrix.setVgap(4);
        for (int row = 0; row < 4; row++) {
            matrix.add(new Label("L" + row), 0, row + 1);
            matrix.add(new Label("L" + row), row + 1, 0);
            for (int col = 0; col < 4; col++) {
                CheckBox c = new CheckBox();
                c.setSelected(true);
                final int r = row;
                final int cc = col;
                c.selectedProperty().addListener((obs, oldValue, selected) -> {
                    if (selected) {
                        masks[r] |= (1 << cc);
                    } else {
                        masks[r] &= ~(1 << cc);
                    }
                    refreshFilters();
                });
                matrix.add(c, col + 1, row + 1);
            }
        }

        root.getChildren().addAll(new Label("Body count"), countSlider, new Label("Layer interaction matrix"), matrix, stats);
        return root;
    }

    private void spawnBodies(int count) {
        world.getChildren().removeIf(node -> node instanceof Box && node.getTranslateY() > -200);
        bodies.removeIf(body -> body.dynamic);

        int clamped = Math.max(8, count);
        for (int i = 0; i < clamped; i++) {
            int layer = i % 4;
            double x = ((i * 41) % 760) - 380;
            double z = ((i * 29) % 320) - 160;
            double y = -180 + (i / 4) * 54;
            Body body = createBody(x, y, z, 38, 38, 38, 1.0, layer);
            body.dynamic = true;
            body.node.setMaterial(new PhongMaterial(LAYER_COLORS[layer]));
            body.velocity = new Vector3D((i % 2 == 0 ? 45 : -45), 0, 0);
            bodies.add(body);
            world.getChildren().add(body.node);
        }
        refreshFilters();
        updateCounters();
    }

    private Body createBody(double x, double y, double z, double sx, double sy, double sz, double invMass, int layer) {
        Body body = new Body();
        body.position = new Vector3D(x, y, z);
        body.velocity = Vector3D.ZERO;
        body.invMass = invMass;
        body.sizeX = sx;
        body.sizeY = sy;
        body.sizeZ = sz;
        body.layer = layer;
        body.filter = new CollisionFilter(1 << layer, masks[layer], org.dynamisfx.collision.CollisionKind.SOLID);
        body.node = new Box(sx, sy, sz);
        body.node.setTranslateX(x);
        body.node.setTranslateY(y);
        body.node.setTranslateZ(z);
        return body;
    }

    private void refreshFilters() {
        for (Body body : bodies) {
            int bit = 1 << body.layer;
            body.filter = new CollisionFilter(bit, masks[body.layer], org.dynamisfx.collision.CollisionKind.SOLID);
        }
    }

    private void updateCounters() {
        Set<CollisionPair<Body>> candidates = broadPhase.findPotentialPairs(bodies, Body::aabb);
        Set<FilteredCollisionPair<Body>> filtered = CollisionFiltering.filterPairs(candidates, b -> b.filter);
        long resolved = filtered.stream().filter(FilteredCollisionPair::responseEnabled).count();
        stats.setText("Pairs tested=" + candidates.size()
                + "  filtered=" + (candidates.size() - filtered.size())
                + "  resolved=" + resolved);
    }

    private static Slider slider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        return slider;
    }

    private static final class Body {
        private Vector3D position;
        private Vector3D velocity;
        private double invMass;
        private double sizeX;
        private double sizeY;
        private double sizeZ;
        private int layer;
        private boolean dynamic;
        private CollisionFilter filter;
        private Box node;

        private Aabb aabb() {
            return new Aabb(
                    position.x - sizeX * 0.5,
                    position.y - sizeY * 0.5,
                    position.z - sizeZ * 0.5,
                    position.x + sizeX * 0.5,
                    position.y + sizeY * 0.5,
                    position.z + sizeZ * 0.5);
        }
    }

    private static final class BodyAdapter implements RigidBodyAdapter3D<Body> {
        @Override
        public Vector3D getPosition(Body body) {
            return body.position;
        }

        @Override
        public void setPosition(Body body, Vector3D position) {
            body.position = position;
            body.node.setTranslateX(position.x);
            body.node.setTranslateY(position.y);
            body.node.setTranslateZ(position.z);
        }

        @Override
        public Vector3D getVelocity(Body body) {
            return body.velocity;
        }

        @Override
        public void setVelocity(Body body, Vector3D velocity) {
            body.velocity = velocity;
        }

        @Override
        public double getInverseMass(Body body) {
            return body.dynamic ? body.invMass : 0.0;
        }

        @Override
        public double getRestitution(Body body) {
            return 0.0;
        }

        @Override
        public double getFriction(Body body) {
            return 0.5;
        }
    }
}
