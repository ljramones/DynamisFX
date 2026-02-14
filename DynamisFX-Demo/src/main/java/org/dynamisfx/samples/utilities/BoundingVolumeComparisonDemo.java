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
import java.util.Optional;
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
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import org.dynamisfx.collision.Aabb;
import org.dynamisfx.collision.BoundingSphere;
import org.dynamisfx.collision.CollisionManifold3D;
import org.dynamisfx.collision.ConvexSupport3D;
import org.dynamisfx.collision.Gjk3D;
import org.dynamisfx.collision.Intersection3D;
import org.dynamisfx.geometry.Vector3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Shows AABB, sphere and convex support approximations side-by-side.
 */
public class BoundingVolumeComparisonDemo extends ShapeBaseSample<Group> {

    private final Group world = new Group();
    private final Group hullLayer = new Group();

    private final Group leftShape = new Group();
    private final Group rightShape = new Group();
    private final Box leftAabb = wireBox(Color.CORNFLOWERBLUE);
    private final Box rightAabb = wireBox(Color.DARKORANGE);
    private final Sphere leftSphere = wireSphere(Color.LIGHTGREEN);
    private final Sphere rightSphere = wireSphere(Color.GOLD);

    private Slider rotation;
    private Slider separation;
    private Label stats = new Label();
    private AnimationTimer timer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1400);
        world.getChildren().clear();
        hullLayer.getChildren().clear();

        buildComposite(leftShape, Color.STEELBLUE);
        buildComposite(rightShape, Color.TOMATO);
        leftShape.setTranslateX(-180);
        rightShape.setTranslateX(180);

        world.getChildren().addAll(leftShape, rightShape, leftAabb, rightAabb, leftSphere, rightSphere, hullLayer);
        model = world;
    }

    @Override
    protected void addMeshAndListeners() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateVolumes();
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
        root.getChildren().add(new Label("Bounding Volume Comparison"));

        rotation = slider(0, 180, 30);
        separation = slider(120, 520, 280);

        root.getChildren().addAll(new Label("Left shape rotation"), rotation,
                new Label("Shape separation"), separation,
                stats);
        return root;
    }

    private void updateVolumes() {
        leftShape.setRotate(value(rotation, 30));
        rightShape.setRotate(-value(rotation, 30) * 0.6);
        rightShape.setTranslateX(value(separation, 280) - 180);

        Aabb a = Aabb.fromBounds(leftShape.getBoundsInParent());
        Aabb b = Aabb.fromBounds(rightShape.getBoundsInParent());
        placeAabb(leftAabb, a);
        placeAabb(rightAabb, b);

        BoundingSphere sa = fromAabb(a);
        BoundingSphere sb = fromAabb(b);
        placeSphere(leftSphere, sa);
        placeSphere(rightSphere, sb);

        ConvexSupport3D ca = compositeSupport(leftShape, 65.0);
        ConvexSupport3D cb = compositeSupport(rightShape, 65.0);
        boolean aabbHit = Intersection3D.intersects(a, b);
        boolean sphereHit = Intersection3D.intersects(sa, sb);
        boolean gjkHit = Gjk3D.intersects(ca, cb);
        Optional<CollisionManifold3D> manifold = Gjk3D.intersectsWithManifold(ca, cb);

        drawHullSamples(ca, cb);
        stats.setText("AABB=" + aabbHit + " | Sphere=" + sphereHit + " | GJK=" + gjkHit
                + (manifold.isPresent() ? " | depth=" + round(manifold.get().penetrationDepth()) : ""));
    }

    private void drawHullSamples(ConvexSupport3D a, ConvexSupport3D b) {
        hullLayer.getChildren().clear();
        List<Vector3D> dirs = sampleDirections();
        for (int i = 0; i < dirs.size(); i++) {
            Vector3D dir = dirs.get(i);
            Vector3D pa = a.support(dir);
            Vector3D pb = b.support(dir);
            Sphere sa = new Sphere(2.5);
            Sphere sb = new Sphere(2.5);
            sa.setMaterial(new PhongMaterial(Color.color(0.2, 0.8, 1.0, 0.6)));
            sb.setMaterial(new PhongMaterial(Color.color(1.0, 0.6, 0.2, 0.6)));
            sa.setTranslateX(pa.x);
            sa.setTranslateY(pa.y);
            sa.setTranslateZ(pa.z);
            sb.setTranslateX(pb.x);
            sb.setTranslateY(pb.y);
            sb.setTranslateZ(pb.z);
            hullLayer.getChildren().addAll(sa, sb);
        }
    }

    private static List<Vector3D> sampleDirections() {
        List<Vector3D> dirs = new ArrayList<>();
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                for (int k = -2; k <= 2; k++) {
                    if (i == 0 && j == 0 && k == 0) {
                        continue;
                    }
                    Vector3D d = new Vector3D(i, j, k);
                    d.normalize();
                    dirs.add(d);
                }
            }
        }
        return dirs;
    }

    private static ConvexSupport3D compositeSupport(Group group, double radius) {
        double cx = group.getBoundsInParent().getCenterX();
        double cy = group.getBoundsInParent().getCenterY();
        double cz = group.getBoundsInParent().getCenterZ();
        return direction -> {
            Vector3D d = new Vector3D(direction);
            d.normalize();
            return new Vector3D(cx + d.x * radius, cy + d.y * radius, cz + d.z * radius);
        };
    }

    private static BoundingSphere fromAabb(Aabb aabb) {
        double dx = aabb.sizeX();
        double dy = aabb.sizeY();
        double dz = aabb.sizeZ();
        double radius = Math.sqrt(dx * dx + dy * dy + dz * dz) * 0.5;
        return new BoundingSphere(aabb.centerX(), aabb.centerY(), aabb.centerZ(), radius);
    }

    private static void placeAabb(Box box, Aabb aabb) {
        box.setTranslateX(aabb.centerX());
        box.setTranslateY(aabb.centerY());
        box.setTranslateZ(aabb.centerZ());
        box.setWidth(Math.max(1, aabb.sizeX()));
        box.setHeight(Math.max(1, aabb.sizeY()));
        box.setDepth(Math.max(1, aabb.sizeZ()));
    }

    private static void placeSphere(Sphere sphere, BoundingSphere b) {
        sphere.setTranslateX(b.centerX());
        sphere.setTranslateY(b.centerY());
        sphere.setTranslateZ(b.centerZ());
        sphere.setRadius(Math.max(1, b.radius()));
    }

    private static void buildComposite(Group target, Color color) {
        target.getChildren().clear();
        Box a = new Box(160, 30, 40);
        Box b = new Box(40, 120, 40);
        Box c = new Box(40, 30, 160);
        a.setTranslateX(-30);
        b.setTranslateY(45);
        c.setTranslateZ(30);
        PhongMaterial material = new PhongMaterial(color);
        a.setMaterial(material);
        b.setMaterial(material);
        c.setMaterial(material);
        target.getChildren().addAll(a, b, c);
    }

    private static Box wireBox(Color color) {
        Box box = new Box(10, 10, 10);
        box.setDrawMode(DrawMode.LINE);
        box.setCullFace(CullFace.NONE);
        box.setMaterial(new PhongMaterial(color));
        return box;
    }

    private static Sphere wireSphere(Color color) {
        Sphere sphere = new Sphere(10);
        sphere.setDrawMode(DrawMode.LINE);
        sphere.setCullFace(CullFace.NONE);
        sphere.setMaterial(new PhongMaterial(color));
        return sphere;
    }

    private static Slider slider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        return slider;
    }

    private static double value(Slider slider, double fallback) {
        return slider == null ? fallback : slider.getValue();
    }

    private static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
