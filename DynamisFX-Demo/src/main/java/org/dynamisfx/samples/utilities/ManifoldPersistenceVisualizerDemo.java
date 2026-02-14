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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import org.dynamisfx.collision.Aabb;
import org.dynamisfx.collision.CollisionEvent;
import org.dynamisfx.collision.CollisionPair;
import org.dynamisfx.collision.CollisionWorld3D;
import org.dynamisfx.collision.ContactGenerator3D;
import org.dynamisfx.collision.ContactPoint3D;
import org.dynamisfx.collision.ContactSolver3D;
import org.dynamisfx.collision.RigidBodyAdapter3D;
import org.dynamisfx.collision.SweepAndPrune3D;
import org.dynamisfx.collision.WarmStartImpulse;
import org.dynamisfx.geometry.Vector3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Visualizes persistent contacts and warm-start impulses during sliding contact.
 */
public class ManifoldPersistenceVisualizerDemo extends ShapeBaseSample<Group> {

    private final Group world = new Group();
    private final Group contactLayer = new Group();
    private final SweepAndPrune3D<Body> broad = new SweepAndPrune3D<>();
    private final BodyAdapter adapter = new BodyAdapter();
    private final ContactSolver3D<Body> solver = new ContactSolver3D<>(adapter);
    private final CollisionWorld3D<Body> collisionWorld = new CollisionWorld3D<>(
            broad,
            Body::aabb,
            b -> org.dynamisfx.collision.CollisionFilter.DEFAULT,
            (a, b) -> ContactGenerator3D.generate(a.aabb(), b.aabb()));

    private final Map<String, Integer> ageByPair = new HashMap<>();
    private final Map<String, WarmStartImpulse> impulseByPair = new HashMap<>();
    private final Label stats = new Label();
    private final CheckBox warmStart = new CheckBox("Warm start enabled");

    private Body floor;
    private Body slider;
    private AnimationTimer timer;
    private double time;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1200);
        world.getChildren().clear();
        contactLayer.getChildren().clear();

        floor = createBody(0, -260, 0, 800, 40, 360, 0.0, false, Color.DIMGRAY);
        slider = createBody(0, -180, 0, 180, 46, 120, 1.0, true, Color.CORNFLOWERBLUE);
        world.getChildren().addAll(floor.node, slider.node, contactLayer);

        collisionWorld.setBodyAdapter(adapter);
        collisionWorld.setResponder(solver);
        collisionWorld.setGravity(new Vector3D(0, -980, 0));
        collisionWorld.setSolverIterations(10);
        collisionWorld.setManifoldRetentionFrames(3);

        model = world;
    }

    @Override
    protected void addMeshAndListeners() {
        warmStart.setSelected(true);
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
                time += dt;

                slider.velocity = new Vector3D(Math.sin(time * 1.2) * 180.0, slider.velocity.y, 0.0);
                collisionWorld.setManifoldRetentionFrames(warmStart.isSelected() ? 3 : 0);
                List<CollisionEvent<Body>> events = collisionWorld.step(List.of(floor, slider), dt);
                renderManifold(events);
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
        root.getChildren().add(new Label("Manifold Persistence Visualizer"));
        root.getChildren().addAll(warmStart, stats);
        return root;
    }

    private void renderManifold(List<CollisionEvent<Body>> events) {
        contactLayer.getChildren().clear();
        Map<String, Integer> nextAges = new HashMap<>();
        for (CollisionEvent<Body> event : events) {
            if (event.manifold() == null || event.manifold().contacts().isEmpty()) {
                continue;
            }
            String key = key(event.pair());
            int age = ageByPair.getOrDefault(key, 0) + 1;
            nextAges.put(key, age);
            WarmStartImpulse impulse = collisionWorld.manifoldCache().getWarmStart(event.pair()).orElse(WarmStartImpulse.ZERO);
            impulseByPair.put(key, impulse);

            for (ContactPoint3D cp : event.manifold().contacts()) {
                Sphere marker = new Sphere(5.0);
                double t = Math.min(1.0, age / 30.0);
                marker.setMaterial(new PhongMaterial(Color.color(1.0 - t, 0.3 + t * 0.7, 1.0 - t)));
                marker.setTranslateX(cp.x());
                marker.setTranslateY(cp.y());
                marker.setTranslateZ(cp.z());
                contactLayer.getChildren().add(marker);

                double impulseLen = Math.min(60.0, Math.abs(impulse.normalImpulse()) * 0.4 + Math.abs(impulse.tangentImpulse()) * 0.4);
                if (impulseLen > 1.0) {
                    Sphere tip = new Sphere(2.5);
                    tip.setMaterial(new PhongMaterial(Color.GOLD));
                    tip.setTranslateX(cp.x());
                    tip.setTranslateY(cp.y() + impulseLen);
                    tip.setTranslateZ(cp.z());
                    contactLayer.getChildren().add(tip);
                }
            }
        }
        ageByPair.clear();
        ageByPair.putAll(nextAges);

        WarmStartImpulse impulse = impulseByPair.getOrDefault(key(new CollisionPair<>(floor, slider)), WarmStartImpulse.ZERO);
        stats.setText("contacts=" + events.size() + " activePairs=" + ageByPair.size()
                + " warmN=" + round(impulse.normalImpulse())
                + " warmT=" + round(impulse.tangentImpulse()));
    }

    private static String key(CollisionPair<Body> pair) {
        return System.identityHashCode(pair.first()) < System.identityHashCode(pair.second())
                ? System.identityHashCode(pair.first()) + ":" + System.identityHashCode(pair.second())
                : System.identityHashCode(pair.second()) + ":" + System.identityHashCode(pair.first());
    }

    private Body createBody(double x, double y, double z,
                            double sx, double sy, double sz,
                            double invMass, boolean dynamic, Color color) {
        Body body = new Body();
        body.position = new Vector3D(x, y, z);
        body.velocity = Vector3D.ZERO;
        body.invMass = invMass;
        body.dynamic = dynamic;
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

    private static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private static final class Body {
        private Vector3D position;
        private Vector3D velocity;
        private double invMass;
        private boolean dynamic;
        private double sizeX;
        private double sizeY;
        private double sizeZ;
        private Box node;

        private Aabb aabb() {
            return new Aabb(position.x - sizeX * 0.5, position.y - sizeY * 0.5, position.z - sizeZ * 0.5,
                    position.x + sizeX * 0.5, position.y + sizeY * 0.5, position.z + sizeZ * 0.5);
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
            return 0.8;
        }
    }
}
