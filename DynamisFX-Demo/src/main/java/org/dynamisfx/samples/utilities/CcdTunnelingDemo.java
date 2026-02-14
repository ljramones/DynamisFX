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
import java.util.OptionalDouble;
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
import org.dynamisfx.collision.Ccd3D;
import org.dynamisfx.geometry.Vector3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Demonstrates tunneling differences between discrete, swept AABB and CCD TOI.
 */
public class CcdTunnelingDemo extends ShapeBaseSample<Group> {

    private static final double START_X = -360.0;
    private static final double WALL_X = 0.0;
    private static final double WALL_HALF_THICKNESS = 4.0;
    private static final double SPHERE_RADIUS = 9.0;
    private static final double RESET_X = 380.0;

    private final Group world = new Group();
    private final List<Sphere> discreteGhosts = new ArrayList<>();
    private final Sphere discreteSphere = laneSphere(Color.CORNFLOWERBLUE, 170.0);
    private final Sphere sweptSphere = laneSphere(Color.DARKORANGE, 0.0);
    private final Sphere ccdSphere = laneSphere(Color.LIMEGREEN, -170.0);
    private final Box sweptVolume = new Box(1.0, 24.0, 24.0);

    private AnimationTimer timer;
    private Slider velocitySlider;
    private Label discreteLabel = new Label();
    private Label sweptLabel = new Label();
    private Label ccdLabel = new Label();

    private double discreteX = START_X;
    private double sweptX = START_X;
    private double ccdX = START_X;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-900);
        world.getChildren().clear();
        Box wallTop = wallAtY(170.0);
        Box wallMid = wallAtY(0.0);
        Box wallBottom = wallAtY(-170.0);
        world.getChildren().addAll(wallTop, wallMid, wallBottom, discreteSphere, sweptSphere, ccdSphere);

        sweptVolume.setDrawMode(DrawMode.FILL);
        sweptVolume.setMaterial(new PhongMaterial(Color.color(1.0, 0.5, 0.1, 0.35)));
        sweptVolume.setCullFace(CullFace.NONE);
        world.getChildren().add(sweptVolume);

        resetAll();
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
                double dt = Math.min((now - last) * 1e-9, 1.0 / 20.0);
                last = now;
                step(dt);
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
        root.getChildren().add(new Label("CCD Tunneling Demo"));
        velocitySlider = new Slider(80.0, 4200.0, 800.0);
        velocitySlider.setShowTickLabels(true);
        velocitySlider.setShowTickMarks(true);
        velocitySlider.valueProperty().addListener((obs, oldValue, newValue) -> resetAll());
        root.getChildren().addAll(new Label("Velocity"), velocitySlider, discreteLabel, sweptLabel, ccdLabel);
        return root;
    }

    private void step(double dt) {
        double velocity = velocitySlider == null ? 800.0 : velocitySlider.getValue();
        double delta = velocity * dt;

        // Discrete only.
        double oldDiscreteX = discreteX;
        discreteX += delta;
        pushDiscreteGhost(oldDiscreteX);
        boolean discreteHit = intersectsWall(discreteX);
        if (discreteHit) {
            discreteLabel.setText("Discrete: overlap detected");
            discreteX = START_X;
            clearGhosts();
        } else if (discreteX > RESET_X) {
            discreteLabel.setText("Discrete: tunneled");
            discreteX = START_X;
            clearGhosts();
        } else {
            discreteLabel.setText("Discrete: moving");
        }
        discreteSphere.setTranslateX(discreteX);

        // Swept AABB.
        Aabb sweptFrom = sphereAabb(sweptX, 0.0);
        Vector3D movement = new Vector3D(delta, 0.0, 0.0);
        OptionalDouble sweptToi = Ccd3D.sweptAabbTimeOfImpact(sweptFrom, movement, wallAabb(0.0));
        if (sweptToi.isPresent()) {
            double toi = sweptToi.getAsDouble();
            sweptX += delta * toi;
            sweptLabel.setText("Swept AABB: TOI=" + pct(toi));
            sweptX = START_X;
        } else {
            sweptX += delta;
            if (sweptX > RESET_X) {
                sweptX = START_X;
            }
            sweptLabel.setText("Swept AABB: no hit");
        }
        sweptSphere.setTranslateX(sweptX);
        sweptVolume.setTranslateY(0.0);
        sweptVolume.setTranslateX(sweptX + delta * 0.5);
        sweptVolume.setWidth(Math.max(2.0, Math.abs(delta) + SPHERE_RADIUS * 2.0));

        // Full CCD segment TOI.
        Vector3D ccdStart = new Vector3D(ccdX, -170.0, 0.0);
        Vector3D ccdEnd = new Vector3D(ccdX + delta, -170.0, 0.0);
        Aabb expandedWall = new Aabb(
                WALL_X - WALL_HALF_THICKNESS - SPHERE_RADIUS,
                -170.0 - 40.0,
                -SPHERE_RADIUS,
                WALL_X + WALL_HALF_THICKNESS + SPHERE_RADIUS,
                -170.0 + 40.0,
                SPHERE_RADIUS);
        OptionalDouble ccdToi = Ccd3D.segmentAabbTimeOfImpact(ccdStart, ccdEnd, expandedWall);
        if (ccdToi.isPresent()) {
            double toi = ccdToi.getAsDouble();
            ccdX += delta * toi;
            ccdLabel.setText("Full CCD: TOI=" + pct(toi));
            ccdX = START_X;
        } else {
            ccdX += delta;
            if (ccdX > RESET_X) {
                ccdX = START_X;
            }
            ccdLabel.setText("Full CCD: no hit");
        }
        ccdSphere.setTranslateX(ccdX);
    }

    private void pushDiscreteGhost(double x) {
        Sphere ghost = new Sphere(SPHERE_RADIUS * 0.45);
        ghost.setMaterial(new PhongMaterial(Color.color(0.6, 0.8, 1.0, 0.5)));
        ghost.setTranslateX(x);
        ghost.setTranslateY(170.0);
        ghost.setCullFace(CullFace.NONE);
        discreteGhosts.add(ghost);
        world.getChildren().add(ghost);
        if (discreteGhosts.size() > 22) {
            Sphere old = discreteGhosts.remove(0);
            world.getChildren().remove(old);
        }
    }

    private void clearGhosts() {
        world.getChildren().removeAll(discreteGhosts);
        discreteGhosts.clear();
    }

    private boolean intersectsWall(double x) {
        Aabb sphere = sphereAabb(x, 170.0);
        return sphere.minX() <= WALL_X + WALL_HALF_THICKNESS && sphere.maxX() >= WALL_X - WALL_HALF_THICKNESS;
    }

    private static Aabb sphereAabb(double centerX, double centerY) {
        return new Aabb(
                centerX - SPHERE_RADIUS,
                centerY - SPHERE_RADIUS,
                -SPHERE_RADIUS,
                centerX + SPHERE_RADIUS,
                centerY + SPHERE_RADIUS,
                SPHERE_RADIUS);
    }

    private static Aabb wallAabb(double y) {
        return new Aabb(
                WALL_X - WALL_HALF_THICKNESS,
                y - 34.0,
                -18.0,
                WALL_X + WALL_HALF_THICKNESS,
                y + 34.0,
                18.0);
    }

    private static Sphere laneSphere(Color color, double y) {
        Sphere sphere = new Sphere(SPHERE_RADIUS);
        sphere.setMaterial(new PhongMaterial(color));
        sphere.setTranslateY(y);
        sphere.setCullFace(CullFace.NONE);
        return sphere;
    }

    private static Box wallAtY(double y) {
        Box wall = new Box(WALL_HALF_THICKNESS * 2.0, 68.0, 36.0);
        wall.setTranslateX(WALL_X);
        wall.setTranslateY(y);
        wall.setMaterial(new PhongMaterial(Color.GRAY));
        return wall;
    }

    private void resetAll() {
        discreteX = START_X;
        sweptX = START_X;
        ccdX = START_X;
        discreteSphere.setTranslateX(discreteX);
        sweptSphere.setTranslateX(sweptX);
        ccdSphere.setTranslateX(ccdX);
        clearGhosts();
    }

    private static String pct(double value) {
        return Math.round(value * 1000.0) / 10.0 + "%";
    }
}
