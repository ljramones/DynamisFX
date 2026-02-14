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
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import org.dynamisfx.collision.Aabb;
import org.dynamisfx.collision.BoundingSphere;
import org.dynamisfx.collision.Intersection3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Stress test for mixed primitive intersection costs.
 */
public class MixedPrimitiveStressTestDemo extends ShapeBaseSample<Group> {

    private static final double HALF = 320.0;

    private final Group world = new Group();
    private final List<PrimitiveBody> bodies = new ArrayList<>();

    private Slider count;
    private Slider sphereRatio;
    private Label stats = new Label();
    private AnimationTimer timer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1300);
        world.getChildren().clear();
        bodies.clear();
        buildBodies(220, 0.5);
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
                double dt = Math.min((now - last) * 1e-9, 1.0 / 30.0);
                last = now;
                stepBodies(dt);
                evaluateCosts();
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
        root.getChildren().add(new Label("Mixed Primitive Stress Test"));

        count = slider(40, 420, 220);
        sphereRatio = slider(0.0, 1.0, 0.5);

        count.valueProperty().addListener((obs, oldValue, newValue) -> rebuild());
        sphereRatio.valueProperty().addListener((obs, oldValue, newValue) -> rebuild());

        root.getChildren().addAll(
                new Label("Total count"), count,
                new Label("Sphere ratio"), sphereRatio,
                stats);
        return root;
    }

    private void rebuild() {
        buildBodies((int) Math.round(count.getValue()), sphereRatio.getValue());
    }

    private void buildBodies(int total, double ratio) {
        world.getChildren().clear();
        bodies.clear();
        int spheres = (int) Math.round(total * ratio);

        for (int i = 0; i < total; i++) {
            boolean asSphere = i < spheres;
            PrimitiveBody body = new PrimitiveBody();
            body.x = ((i * 41) % 620) - 310;
            body.y = ((i * 29) % 420) - 210;
            body.z = ((i * 17) % 300) - 150;
            body.vx = ((i & 1) == 0) ? 60 : -60;
            body.vy = ((i % 3) == 0) ? -40 : 40;
            body.vz = ((i % 5) == 0) ? 50 : -50;
            if (asSphere) {
                body.type = Type.SPHERE;
                body.radius = 8 + (i % 5) * 2;
                Sphere node = new Sphere(body.radius);
                node.setMaterial(new PhongMaterial(Color.DARKSEAGREEN));
                body.node = node;
                world.getChildren().add(node);
            } else {
                body.type = Type.BOX;
                body.sx = 14 + (i % 4) * 4;
                body.sy = 14 + (i % 6) * 3;
                body.sz = 14 + (i % 3) * 6;
                Box node = new Box(body.sx, body.sy, body.sz);
                node.setMaterial(new PhongMaterial(Color.SLATEBLUE));
                body.node = node;
                world.getChildren().add(node);
            }
            bodies.add(body);
            syncNode(body);
        }
    }

    private void stepBodies(double dt) {
        for (PrimitiveBody body : bodies) {
            body.x += body.vx * dt;
            body.y += body.vy * dt;
            body.z += body.vz * dt;
            double r = body.type == Type.SPHERE ? body.radius : Math.max(body.sx, Math.max(body.sy, body.sz)) * 0.5;
            if (body.x < -HALF + r || body.x > HALF - r) {
                body.vx = -body.vx;
            }
            if (body.y < -HALF + r || body.y > HALF - r) {
                body.vy = -body.vy;
            }
            if (body.z < -HALF + r || body.z > HALF - r) {
                body.vz = -body.vz;
            }
            syncNode(body);
        }
    }

    private void evaluateCosts() {
        long aabbAabbNanos = 0;
        long sphereSphereNanos = 0;
        long sphereAabbNanos = 0;
        int aabbAabbCalls = 0;
        int sphereSphereCalls = 0;
        int sphereAabbCalls = 0;

        int hits = 0;
        for (int i = 0; i < bodies.size(); i++) {
            PrimitiveBody a = bodies.get(i);
            for (int j = i + 1; j < bodies.size(); j++) {
                PrimitiveBody b = bodies.get(j);
                if (a.type == Type.BOX && b.type == Type.BOX) {
                    long t0 = System.nanoTime();
                    boolean hit = Intersection3D.intersects(a.aabb(), b.aabb());
                    aabbAabbNanos += System.nanoTime() - t0;
                    aabbAabbCalls++;
                    if (hit) {
                        hits++;
                    }
                } else if (a.type == Type.SPHERE && b.type == Type.SPHERE) {
                    long t0 = System.nanoTime();
                    boolean hit = Intersection3D.intersects(a.sphere(), b.sphere());
                    sphereSphereNanos += System.nanoTime() - t0;
                    sphereSphereCalls++;
                    if (hit) {
                        hits++;
                    }
                } else {
                    PrimitiveBody s = a.type == Type.SPHERE ? a : b;
                    PrimitiveBody bx = a.type == Type.BOX ? a : b;
                    long t0 = System.nanoTime();
                    boolean hit = Intersection3D.intersects(s.sphere(), bx.aabb());
                    sphereAabbNanos += System.nanoTime() - t0;
                    sphereAabbCalls++;
                    if (hit) {
                        hits++;
                    }
                }
            }
        }

        stats.setText("hits=" + hits
                + " | AABB-AABB: calls=" + aabbAabbCalls + " avgNs=" + avg(aabbAabbNanos, aabbAabbCalls)
                + " | Sphere-Sphere: calls=" + sphereSphereCalls + " avgNs=" + avg(sphereSphereNanos, sphereSphereCalls)
                + " | Sphere-AABB: calls=" + sphereAabbCalls + " avgNs=" + avg(sphereAabbNanos, sphereAabbCalls));
    }

    private static long avg(long nanos, int calls) {
        return calls == 0 ? 0 : nanos / calls;
    }

    private static void syncNode(PrimitiveBody body) {
        body.node.setTranslateX(body.x);
        body.node.setTranslateY(body.y);
        body.node.setTranslateZ(body.z);
    }

    private static Slider slider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        return slider;
    }

    private enum Type {
        BOX,
        SPHERE
    }

    private static final class PrimitiveBody {
        private Type type;
        private Node node;
        private double x;
        private double y;
        private double z;
        private double vx;
        private double vy;
        private double vz;

        private double sx;
        private double sy;
        private double sz;
        private double radius;

        private Aabb aabb() {
            return new Aabb(x - sx * 0.5, y - sy * 0.5, z - sz * 0.5,
                    x + sx * 0.5, y + sy * 0.5, z + sz * 0.5);
        }

        private BoundingSphere sphere() {
            return new BoundingSphere(x, y, z, radius);
        }
    }
}
