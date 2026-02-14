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

package org.dynamisfx.samples.collision;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import org.dynamisfx.collision.Aabb;
import org.dynamisfx.collision.CollisionFilter;
import org.dynamisfx.collision.CollisionFiltering;
import org.dynamisfx.collision.CollisionPair;
import org.dynamisfx.collision.CollisionPipeline;
import org.dynamisfx.collision.CollisionWorld3D;
import org.dynamisfx.collision.ContactGenerator3D;
import org.dynamisfx.collision.ContactManifold3D;
import org.dynamisfx.collision.ContactSolver3D;
import org.dynamisfx.collision.FilteredCollisionPair;
import org.dynamisfx.collision.RigidBodyAdapter3D;
import org.dynamisfx.collision.SweepAndPrune3D;
import org.dynamisfx.geometry.Vector3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * End-to-end pipeline telemetry monitor.
 */
public class CollisionPipelineMonitorDemo extends ShapeBaseSample<Group> {

    private final Group world = new Group();
    private final SweepAndPrune3D<Body> broad = new SweepAndPrune3D<>();
    private final BodyAdapter adapter = new BodyAdapter();
    private final ContactSolver3D<Body> solver = new ContactSolver3D<>(adapter);
    private final CollisionWorld3D<Body> collisionWorld = new CollisionWorld3D<>(
            broad,
            Body::aabb,
            b -> CollisionFilter.DEFAULT,
            (a, b) -> ContactGenerator3D.generate(a.aabb(), b.aabb()));

    private final List<Body> bodies = new ArrayList<>();
    private final Box broadBar = bar(Color.CORNFLOWERBLUE, -210);
    private final Box filterBar = bar(Color.DARKORANGE, -170);
    private final Box narrowBar = bar(Color.LIMEGREEN, -130);
    private final Box manifoldBar = bar(Color.GOLD, -90);

    private Slider bodyCount;
    private CheckBox paused;
    private Label stats = new Label();
    private AnimationTimer timer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1500);
        world.getChildren().clear();
        bodies.clear();

        Body floor = createBody(0, -300, 0, 1200, 40, 500, 0.0);
        floor.dynamic = false;
        floor.node.setMaterial(new PhongMaterial(Color.GRAY));
        bodies.add(floor);
        world.getChildren().add(floor.node);

        collisionWorld.setBodyAdapter(adapter);
        collisionWorld.setResponder(solver);
        collisionWorld.setGravity(new Vector3D(0, -900, 0));
        collisionWorld.setSolverIterations(8);

        spawnBodies(200);
        world.getChildren().addAll(broadBar, filterBar, narrowBar, manifoldBar);
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
                if (paused != null && paused.isSelected()) {
                    updatePipelineOnly();
                    return;
                }
                collisionWorld.step(bodies, dt);
                updatePipelineOnly();
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
        root.getChildren().add(new Label("Collision Pipeline Monitor"));

        bodyCount = slider(40, 320, 200);
        bodyCount.valueProperty().addListener((obs, oldValue, newValue) -> spawnBodies(newValue.intValue()));

        paused = new CheckBox("Pause pipeline motion");

        root.getChildren().addAll(new Label("Body count"), bodyCount, paused, stats,
                new Label("Flow bars: Broad -> Filter -> Narrow -> Manifold"));
        return root;
    }

    private void spawnBodies(int count) {
        world.getChildren().removeIf(node -> node instanceof Box && node.getTranslateY() > -260);
        bodies.removeIf(body -> body.dynamic);

        int clamped = Math.max(20, count);
        for (int i = 0; i < clamped; i++) {
            double x = ((i * 47) % 1000) - 500;
            double z = ((i * 31) % 380) - 190;
            double y = -220 + (i / 20) * 52;
            Body b = createBody(x, y, z, 26, 26, 26, 1.0);
            b.dynamic = true;
            b.velocity = new Vector3D((i % 2 == 0 ? 35 : -35), 0, 0);
            b.node.setMaterial(new PhongMaterial(i % 3 == 0 ? Color.CADETBLUE : Color.MEDIUMPURPLE));
            bodies.add(b);
            world.getChildren().add(b.node);
        }
        updatePipelineOnly();
    }

    private void updatePipelineOnly() {
        Set<CollisionPair<Body>> candidates = broad.findPotentialPairs(bodies, Body::aabb);
        Set<FilteredCollisionPair<Body>> filtered = CollisionFiltering.filterPairs(candidates, b -> CollisionFilter.DEFAULT);
        Set<CollisionPair<Body>> narrow = CollisionPipeline.findCollisions(candidates,
                (a, b) -> org.dynamisfx.collision.Intersection3D.intersects(a.aabb(), b.aabb()));

        int manifoldCount = 0;
        for (CollisionPair<Body> pair : narrow) {
            Optional<ContactManifold3D> manifold = ContactGenerator3D.generate(pair.first().aabb(), pair.second().aabb());
            if (manifold.isPresent()) {
                manifoldCount += manifold.get().contacts().size();
            }
        }

        setBarWidth(broadBar, candidates.size());
        setBarWidth(filterBar, filtered.size());
        setBarWidth(narrowBar, narrow.size());
        setBarWidth(manifoldBar, manifoldCount);

        stats.setText("broad=" + candidates.size()
                + " | filtered=" + filtered.size()
                + " | narrow=" + narrow.size()
                + " | manifolds=" + manifoldCount);
    }

    private Body createBody(double x, double y, double z, double sx, double sy, double sz, double invMass) {
        Body body = new Body();
        body.position = new Vector3D(x, y, z);
        body.velocity = Vector3D.ZERO;
        body.invMass = invMass;
        body.sizeX = sx;
        body.sizeY = sy;
        body.sizeZ = sz;
        body.node = new Box(sx, sy, sz);
        body.node.setTranslateX(x);
        body.node.setTranslateY(y);
        body.node.setTranslateZ(z);
        return body;
    }

    private static Box bar(Color color, double y) {
        Box bar = new Box(2, 24, 20);
        bar.setMaterial(new PhongMaterial(color));
        bar.setTranslateX(-560);
        bar.setTranslateY(y);
        return bar;
    }

    private static void setBarWidth(Box bar, int count) {
        bar.setWidth(Math.max(2.0, count * 1.8));
        bar.setTranslateX(-560 + bar.getWidth() * 0.5);
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
        private double sizeX;
        private double sizeY;
        private double sizeZ;
        private boolean dynamic;
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
