/*
 * Copyright 2024-2026 DynamisFX Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dynamisfx.samples.utilities;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import org.dynamisfx.collision.Aabb;
import org.dynamisfx.collision.CollisionEvent;
import org.dynamisfx.collision.CollisionWorld3D;
import org.dynamisfx.collision.ContactGenerator3D;
import org.dynamisfx.collision.ContactSolver3D;
import org.dynamisfx.collision.CollisionFilter;
import org.dynamisfx.collision.RigidBodyAdapter3D;
import org.dynamisfx.collision.SweepAndPrune3D;
import org.dynamisfx.geometry.Vector3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Contact solver stack stability demo with tuning controls.
 */
public class ContactSolverStackingDemo extends ShapeBaseSample<Group> {

    private static final double BOX_SIZE = 42.0;
    private static final double FLOOR_Y = -260.0;
    private static final int MAX_BOXES = 20;

    private final Group world = new Group();
    private final List<Body> bodies = new ArrayList<>();
    private final BodyAdapter adapter = new BodyAdapter();
    private final CollisionWorld3D<Body> collisionWorld = new CollisionWorld3D<>(
            new SweepAndPrune3D<>(),
            body -> body.aabb(),
            body -> CollisionFilter.DEFAULT,
            (a, b) -> ContactGenerator3D.generate(a.aabb(), b.aabb()));
    private final ContactSolver3D<Body> solver = new ContactSolver3D<>(adapter);

    private AnimationTimer timer;
    private Slider solverIterations;
    private Slider friction;
    private Slider baumgarte;
    private CheckBox warmStart;
    private Label stats = new Label();

    private Body floor;
    private double dropTimer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1200);
        world.getChildren().clear();
        bodies.clear();

        floor = createBody(0.0, FLOOR_Y, 0.0, 720.0, 40.0, 420.0, 0.0, Color.DIMGRAY);
        floor.dynamic = false;
        bodies.add(floor);
        world.getChildren().add(floor.node);

        collisionWorld.setBodyAdapter(adapter);
        collisionWorld.setResponder(solver);
        collisionWorld.setGravity(new Vector3D(0.0, -980.0, 0.0));
        collisionWorld.setSolverIterations(8);
        collisionWorld.setManifoldRetentionFrames(2);

        for (int i = 0; i < 6; i++) {
            spawnBox();
        }

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
                applySettings();
                List<CollisionEvent<Body>> events = collisionWorld.step(bodies, dt);
                applyPenetrationHeat(events);
                dropTimer += dt;
                if (dropTimer > 0.9 && bodies.size() - 1 < MAX_BOXES) {
                    dropTimer = 0.0;
                    spawnBox();
                }
                stats.setText("Stack boxes=" + (bodies.size() - 1) + "  contacts=" + events.size());
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
        root.getChildren().add(new Label("Contact Solver Stacking"));

        solverIterations = slider(1, 30, 8);
        friction = slider(0.0, 1.2, 0.45);
        baumgarte = slider(0.1, 1.0, 0.8);
        warmStart = new CheckBox("Warm starting");
        warmStart.setSelected(true);

        Button kick = new Button("Kick Bottom Box");
        kick.setOnAction(event -> kickBottom());

        Button reset = new Button("Reset Stack");
        reset.setOnAction(event -> createMesh());

        root.getChildren().addAll(
                new Label("Solver Iterations"), solverIterations,
                new Label("Friction"), friction,
                new Label("Baumgarte Bias"), baumgarte,
                warmStart,
                kick,
                reset,
                stats);
        return root;
    }

    private void applySettings() {
        collisionWorld.setSolverIterations((int) Math.round(solverIterations.getValue()));
        solver.setPositionCorrectionPercent(baumgarte.getValue());
        collisionWorld.setManifoldRetentionFrames(warmStart.isSelected() ? 2 : 0);
        for (Body body : bodies) {
            body.friction = friction.getValue();
        }
    }

    private void applyPenetrationHeat(List<CollisionEvent<Body>> events) {
        for (Body body : bodies) {
            if (!body.dynamic) {
                continue;
            }
            body.node.setMaterial(new PhongMaterial(Color.web("#7fc6ff")));
        }
        for (CollisionEvent<Body> event : events) {
            if (event.manifold() == null) {
                continue;
            }
            double depth = Math.min(1.0, event.manifold().manifold().penetrationDepth() / 8.0);
            Color heat = Color.color(1.0, 1.0 - depth, 1.0 - depth);
            event.pair().first().node.setMaterial(new PhongMaterial(heat));
            event.pair().second().node.setMaterial(new PhongMaterial(heat));
        }
    }

    private void kickBottom() {
        Body target = null;
        double minY = Double.POSITIVE_INFINITY;
        for (Body body : bodies) {
            if (!body.dynamic) {
                continue;
            }
            if (body.position.getY() < minY) {
                minY = body.position.getY();
                target = body;
            }
        }
        if (target != null) {
            target.velocity = new Vector3D(260.0, target.velocity.getY(), 0.0);
        }
    }

    private void spawnBox() {
        double y = FLOOR_Y + 80.0 + (bodies.size() - 1) * (BOX_SIZE + 2.0);
        Body body = createBody(0.0, y, 0.0, BOX_SIZE, BOX_SIZE, BOX_SIZE, 1.0, Color.web("#7fc6ff"));
        body.dynamic = true;
        body.friction = friction == null ? 0.45 : friction.getValue();
        bodies.add(body);
        world.getChildren().add(body.node);
    }

    private Body createBody(double x, double y, double z, double sx, double sy, double sz, double invMass, Color color) {
        Body body = new Body();
        body.position = new Vector3D(x, y, z);
        body.velocity = new Vector3D(0.0, 0.0, 0.0);
        body.invMass = invMass;
        body.friction = 0.45;
        body.restitution = 0.0;
        body.sizeX = sx;
        body.sizeY = sy;
        body.sizeZ = sz;
        body.node = new Box(sx, sy, sz);
        body.node.setTranslateX(x);
        body.node.setTranslateY(y);
        body.node.setTranslateZ(z);
        body.node.setMaterial(new PhongMaterial(color));
        return body;
    }

    private static Slider slider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        return slider;
    }

    private static final class Body {
        private Vector3D position;
        private Vector3D velocity;
        private double invMass;
        private double restitution;
        private double friction;
        private double sizeX;
        private double sizeY;
        private double sizeZ;
        private boolean dynamic;
        private Box node;

        private Aabb aabb() {
            return new Aabb(
                    position.getX() - sizeX * 0.5,
                    position.getY() - sizeY * 0.5,
                    position.getZ() - sizeZ * 0.5,
                    position.getX() + sizeX * 0.5,
                    position.getY() + sizeY * 0.5,
                    position.getZ() + sizeZ * 0.5);
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
            body.node.setTranslateX(position.getX());
            body.node.setTranslateY(position.getY());
            body.node.setTranslateZ(position.getZ());
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
            return body.restitution;
        }

        @Override
        public double getFriction(Body body) {
            return body.friction;
        }
    }
}
