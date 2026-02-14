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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import org.dynamisfx.collision.Aabb;
import org.dynamisfx.collision.CollisionDebugSnapshot3D;
import org.dynamisfx.collision.CollisionEvent;
import org.dynamisfx.collision.CollisionPair;
import org.dynamisfx.collision.CollisionWorld3D;
import org.dynamisfx.collision.NodeCollisionAdapter;
import org.dynamisfx.collision.SweepAndPrune3D;
import org.dynamisfx.geometry.Vector3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Runtime collision-world demo with wireframe AABB/contact debug overlays.
 */
public class CollisionDebugWorld extends ShapeBaseSample<Group> {

    private static final double HALF_EXTENT = 320.0;

    private final PhongMaterial bodyMaterial = new PhongMaterial(Color.CORNFLOWERBLUE);
    private final PhongMaterial collidingMaterial = new PhongMaterial(Color.ORANGERED);
    private final PhongMaterial boundsMaterial = new PhongMaterial(Color.GOLD);
    private final PhongMaterial enterContactMaterial = new PhongMaterial(Color.LIMEGREEN);
    private final PhongMaterial stayContactMaterial = new PhongMaterial(Color.GOLD);
    private final PhongMaterial exitContactMaterial = new PhongMaterial(Color.GRAY);

    private final List<Box> bodies = new ArrayList<>();
    private final List<Vector3D> velocities = new ArrayList<>();
    private Group debugOverlay;
    private Group worldGroup;

    private CollisionWorld3D<Node> world;
    private AnimationTimer timer;
    private boolean paused;
    private boolean debugVisible = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1300);
        bodies.clear();
        velocities.clear();
        worldGroup = new Group();
        debugOverlay = new Group();

        world = CollisionWorld3D.forJavaFxNodes(new SweepAndPrune3D<>());
        spawnBody(-220, -120, -80, 120, 90, 70, 85, 65, 70);
        spawnBody(-40, 40, 50, 130, 80, 110, -70, 55, -45);
        spawnBody(170, -30, 120, 95, 140, 75, -90, -80, 40);
        spawnBody(210, 170, -130, 110, 95, 120, 75, -70, 95);

        model = new Group(worldGroup, debugOverlay);
    }

    @Override
    protected void addMeshAndListeners() {
        subScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                paused = !paused;
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.D) {
                debugVisible = !debugVisible;
                debugOverlay.setVisible(debugVisible);
                event.consume();
            }
        });

        timer = new AnimationTimer() {
            private long lastTick;

            @Override
            public void handle(long now) {
                if (lastTick == 0L) {
                    lastTick = now;
                    return;
                }
                double dt = Math.min((now - lastTick) * 1.0e-9, 1.0 / 30.0);
                lastTick = now;
                if (paused) {
                    return;
                }
                stepBodies(dt);
                renderFrame(world.update(List.copyOf(bodies)));
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
        return null;
    }

    private void spawnBody(
            double x, double y, double z,
            double sx, double sy, double sz,
            double vx, double vy, double vz) {
        Box box = new Box(sx, sy, sz);
        box.setMaterial(bodyMaterial);
        box.setCullFace(CullFace.NONE);
        box.setTranslateX(x);
        box.setTranslateY(y);
        box.setTranslateZ(z);
        NodeCollisionAdapter.setFilter(box, org.dynamisfx.collision.CollisionFilter.DEFAULT);
        bodies.add(box);
        velocities.add(new Vector3D(vx, vy, vz));
        worldGroup.getChildren().add(box);
    }

    private void stepBodies(double dt) {
        for (int i = 0; i < bodies.size(); i++) {
            Box box = bodies.get(i);
            Vector3D v = velocities.get(i);
            double x = box.getTranslateX() + v.x * dt;
            double y = box.getTranslateY() + v.y * dt;
            double z = box.getTranslateZ() + v.z * dt;

            double halfX = box.getWidth() * 0.5;
            double halfY = box.getHeight() * 0.5;
            double halfZ = box.getDepth() * 0.5;

            if (x - halfX < -HALF_EXTENT || x + halfX > HALF_EXTENT) {
                v = new Vector3D(-v.x, v.y, v.z);
                x = clamp(x, -HALF_EXTENT + halfX, HALF_EXTENT - halfX);
            }
            if (y - halfY < -HALF_EXTENT || y + halfY > HALF_EXTENT) {
                v = new Vector3D(v.x, -v.y, v.z);
                y = clamp(y, -HALF_EXTENT + halfY, HALF_EXTENT - halfY);
            }
            if (z - halfZ < -HALF_EXTENT || z + halfZ > HALF_EXTENT) {
                v = new Vector3D(v.x, v.y, -v.z);
                z = clamp(z, -HALF_EXTENT + halfZ, HALF_EXTENT - halfZ);
            }

            velocities.set(i, v);
            box.setTranslateX(x);
            box.setTranslateY(y);
            box.setTranslateZ(z);
        }
    }

    private void renderFrame(List<CollisionEvent<Node>> events) {
        CollisionDebugSnapshot3D<Node> snapshot = CollisionDebugSnapshot3D.from(
                List.copyOf(bodies),
                NodeCollisionAdapter::boundsInParent,
                events);

        Set<Node> colliding = new HashSet<>();
        for (CollisionEvent<Node> event : events) {
            if (event.type() != org.dynamisfx.collision.CollisionEventType.EXIT) {
                CollisionPair<Node> pair = event.pair();
                colliding.add(pair.first());
                colliding.add(pair.second());
            }
        }

        for (Box body : bodies) {
            body.setMaterial(colliding.contains(body) ? collidingMaterial : bodyMaterial);
        }

        debugOverlay.getChildren().clear();
        if (!debugVisible) {
            return;
        }

        for (CollisionDebugSnapshot3D.ItemBounds<Node> item : snapshot.items()) {
            debugOverlay.getChildren().add(buildWireAabb(item.bounds()));
        }
        for (CollisionDebugSnapshot3D.Contact<Node> contact : snapshot.contacts()) {
            Sphere marker = new Sphere(6.0);
            marker.setCullFace(CullFace.NONE);
            marker.setDrawMode(DrawMode.FILL);
            marker.setTranslateX(contact.point().x());
            marker.setTranslateY(contact.point().y());
            marker.setTranslateZ(contact.point().z());
            marker.setMaterial(switch (contact.type()) {
                case ENTER -> enterContactMaterial;
                case STAY -> stayContactMaterial;
                case EXIT -> exitContactMaterial;
            });
            debugOverlay.getChildren().add(marker);
        }
    }

    private Box buildWireAabb(Aabb bounds) {
        double width = bounds.maxX() - bounds.minX();
        double height = bounds.maxY() - bounds.minY();
        double depth = bounds.maxZ() - bounds.minZ();
        Box box = new Box(width, height, depth);
        box.setCullFace(CullFace.NONE);
        box.setDrawMode(DrawMode.LINE);
        box.setMaterial(boundsMaterial);
        box.setTranslateX(bounds.minX() + width * 0.5);
        box.setTranslateY(bounds.minY() + height * 0.5);
        box.setTranslateZ(bounds.minZ() + depth * 0.5);
        return box;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
