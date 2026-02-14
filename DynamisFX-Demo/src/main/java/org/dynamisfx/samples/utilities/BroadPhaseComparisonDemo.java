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
import java.util.Random;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
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
import org.dynamisfx.collision.CollisionPair;
import org.dynamisfx.collision.Intersection3D;
import org.dynamisfx.collision.SpatialHash3D;
import org.dynamisfx.collision.SweepAndPrune3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Spatial hash vs sweep-and-prune broad-phase comparison.
 */
public class BroadPhaseComparisonDemo extends ShapeBaseSample<Group> {

    private static final double HALF_EXTENT = 320.0;
    private static final double VIEW_OFFSET_X = 520.0;
    private static final double RADIUS = 8.0;
    private static final double CELL_SIZE = 80.0;
    private static final int MIN_BODIES = 100;
    private static final int MAX_BODIES = 5000;

    private final SpatialHash3D<MovingSphere> spatialHash = new SpatialHash3D<>(CELL_SIZE);
    private final SweepAndPrune3D<MovingSphere> sweepAndPrune = new SweepAndPrune3D<>();
    private final List<MovingSphere> bodies = new ArrayList<>();
    private final Random random = new Random(7L);
    private final Group world = new Group();
    private final Group gridOverlay = new Group();
    private final Box sapActiveBar = new Box(2, 16, 16);

    private AnimationTimer timer;
    private Slider countSlider;
    private ComboBox<Distribution> distributionBox;
    private Label hashStats = new Label();
    private Label sapStats = new Label();
    private Label frameStats = new Label();
    private String pendingHashStats = "";
    private String pendingSapStats = "";
    private String pendingFrameStats = "";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1700);
        world.getChildren().clear();
        gridOverlay.getChildren().clear();
        buildStaticVisuals();
        rebuildBodies(MIN_BODIES, Distribution.UNIFORM);
        model = world;
    }

    @Override
    protected void addMeshAndListeners() {
        timer = new AnimationTimer() {
            private long last = 0L;

            @Override
            public void handle(long now) {
                if (last == 0L) {
                    last = now;
                    return;
                }
                double dt = Math.min((now - last) * 1e-9, 1.0 / 20.0);
                last = now;
                stepBodies(dt);
                updateStats();
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
        root.getChildren().add(new Label("Broad Phase Comparison"));

        countSlider = new Slider(MIN_BODIES, MAX_BODIES, MIN_BODIES);
        countSlider.setShowTickLabels(true);
        countSlider.setShowTickMarks(true);
        countSlider.setMajorTickUnit(1000);
        countSlider.valueProperty().addListener((obs, oldValue, newValue) -> rebuildBodies(
                newValue.intValue(),
                selectedDistribution()));

        distributionBox = new ComboBox<>();
        distributionBox.getItems().setAll(Distribution.values());
        distributionBox.setValue(Distribution.UNIFORM);
        distributionBox.valueProperty().addListener((obs, oldValue, newValue) -> rebuildBodies(
                (int) Math.round(countSlider.getValue()),
                selectedDistribution()));

        root.getChildren().addAll(
                new Label("Body Count"),
                countSlider,
                new Label("Distribution"),
                distributionBox,
                hashStats,
                sapStats,
                frameStats);
        return root;
    }

    private void buildStaticVisuals() {
        Box leftBounds = wireCube(HALF_EXTENT * 2.0, Color.DEEPSKYBLUE);
        leftBounds.setTranslateX(-VIEW_OFFSET_X);
        Box rightBounds = wireCube(HALF_EXTENT * 2.0, Color.ORANGE);
        rightBounds.setTranslateX(VIEW_OFFSET_X);
        world.getChildren().addAll(leftBounds, rightBounds);

        buildGridOverlay();
        world.getChildren().add(gridOverlay);

        sapActiveBar.setMaterial(new PhongMaterial(Color.GOLD));
        sapActiveBar.setTranslateX(VIEW_OFFSET_X);
        sapActiveBar.setTranslateY(-HALF_EXTENT - 60.0);
        world.getChildren().add(sapActiveBar);
    }

    private void buildGridOverlay() {
        int halfCells = (int) Math.ceil(HALF_EXTENT / CELL_SIZE);
        for (int x = -halfCells; x <= halfCells; x++) {
            for (int y = -halfCells; y <= halfCells; y++) {
                if ((Math.abs(x) + Math.abs(y)) > halfCells + 1) {
                    continue;
                }
                Box cell = wireCube(CELL_SIZE, Color.DARKCYAN);
                cell.setTranslateX(-VIEW_OFFSET_X + x * CELL_SIZE);
                cell.setTranslateY(y * CELL_SIZE);
                gridOverlay.getChildren().add(cell);
            }
        }
        gridOverlay.setOpacity(0.22);
    }

    private void rebuildBodies(int count, Distribution distribution) {
        int clamped = Math.max(MIN_BODIES, Math.min(MAX_BODIES, count));
        world.getChildren().removeIf(node -> node.getUserData() == bodies);
        bodies.clear();

        for (int i = 0; i < clamped; i++) {
            double x;
            double y;
            double z;
            switch (distribution) {
                case CLUSTERED -> {
                    double cx = randomRange(-120.0, 120.0);
                    double cy = randomRange(-120.0, 120.0);
                    x = cx + randomRange(-40.0, 40.0);
                    y = cy + randomRange(-40.0, 40.0);
                    z = randomRange(-90.0, 90.0);
                }
                case PLANAR -> {
                    x = randomRange(-HALF_EXTENT + 20.0, HALF_EXTENT - 20.0);
                    y = randomRange(-HALF_EXTENT + 20.0, HALF_EXTENT - 20.0);
                    z = randomRange(-20.0, 20.0);
                }
                default -> {
                    x = randomRange(-HALF_EXTENT + 20.0, HALF_EXTENT - 20.0);
                    y = randomRange(-HALF_EXTENT + 20.0, HALF_EXTENT - 20.0);
                    z = randomRange(-HALF_EXTENT + 20.0, HALF_EXTENT - 20.0);
                }
            }
            double vx = randomRange(-130.0, 130.0);
            double vy = randomRange(-130.0, 130.0);
            double vz = randomRange(-130.0, 130.0);

            Sphere left = new Sphere(RADIUS);
            left.setMaterial(new PhongMaterial(Color.web("#5bc0ff")));
            left.setCullFace(CullFace.NONE);
            left.setTranslateX(-VIEW_OFFSET_X + x);
            left.setTranslateY(y);
            left.setTranslateZ(z);
            left.setUserData(bodies);

            Sphere right = new Sphere(RADIUS);
            right.setMaterial(new PhongMaterial(Color.web("#ff9c52")));
            right.setCullFace(CullFace.NONE);
            right.setTranslateX(VIEW_OFFSET_X + x);
            right.setTranslateY(y);
            right.setTranslateZ(z);
            right.setUserData(bodies);

            MovingSphere body = new MovingSphere(x, y, z, vx, vy, vz, left, right);
            bodies.add(body);
            world.getChildren().addAll(left, right);
        }
        updateStats();
    }

    private void stepBodies(double dt) {
        for (MovingSphere body : bodies) {
            body.x += body.vx * dt;
            body.y += body.vy * dt;
            body.z += body.vz * dt;

            if (body.x < -HALF_EXTENT + RADIUS || body.x > HALF_EXTENT - RADIUS) {
                body.vx = -body.vx;
                body.x = clamp(body.x, -HALF_EXTENT + RADIUS, HALF_EXTENT - RADIUS);
            }
            if (body.y < -HALF_EXTENT + RADIUS || body.y > HALF_EXTENT - RADIUS) {
                body.vy = -body.vy;
                body.y = clamp(body.y, -HALF_EXTENT + RADIUS, HALF_EXTENT - RADIUS);
            }
            if (body.z < -HALF_EXTENT + RADIUS || body.z > HALF_EXTENT - RADIUS) {
                body.vz = -body.vz;
                body.z = clamp(body.z, -HALF_EXTENT + RADIUS, HALF_EXTENT - RADIUS);
            }

            body.left.setTranslateX(-VIEW_OFFSET_X + body.x);
            body.left.setTranslateY(body.y);
            body.left.setTranslateZ(body.z);
            body.right.setTranslateX(VIEW_OFFSET_X + body.x);
            body.right.setTranslateY(body.y);
            body.right.setTranslateZ(body.z);
        }
    }

    private void updateStats() {
        long t0 = System.nanoTime();
        Set<CollisionPair<MovingSphere>> hashCandidates = spatialHash.findPotentialPairs(bodies, this::aabbOf);
        int hashNarrow = countSphereIntersections(hashCandidates);
        long t1 = System.nanoTime();
        Set<CollisionPair<MovingSphere>> sapCandidates = sweepAndPrune.findPotentialPairs(bodies, this::aabbOf);
        int sapNarrow = countSphereIntersections(sapCandidates);
        int sapActivePeak = estimateSweepActivePeak();
        long t2 = System.nanoTime();

        pendingHashStats = "SpatialHash  candidates=" + hashCandidates.size() + "  narrow=" + hashNarrow
                + "  ms=" + ms(t1 - t0);
        pendingSapStats = "SweepPrune  candidates=" + sapCandidates.size() + "  narrow=" + sapNarrow
                + "  ms=" + ms(t2 - t1);
        pendingFrameStats = "Bodies=" + bodies.size() + "  distribution=" + selectedDistribution().name().toLowerCase()
                + "  sweepPeak=" + sapActivePeak;
        if (Platform.isFxApplicationThread()) {
            hashStats.setText(pendingHashStats);
            sapStats.setText(pendingSapStats);
            frameStats.setText(pendingFrameStats);
        }

        sapActiveBar.setWidth(Math.max(2.0, sapActivePeak * 2.0));
    }

    private int countSphereIntersections(Set<CollisionPair<MovingSphere>> candidates) {
        int count = 0;
        for (CollisionPair<MovingSphere> pair : candidates) {
            if (Intersection3D.intersects(sphereOf(pair.first()), sphereOf(pair.second()))) {
                count++;
            }
        }
        return count;
    }

    private int estimateSweepActivePeak() {
        List<MovingSphere> sorted = new ArrayList<>(bodies);
        sorted.sort((a, b) -> Double.compare(a.x - RADIUS, b.x - RADIUS));
        List<MovingSphere> active = new ArrayList<>();
        int peak = 0;
        for (MovingSphere body : sorted) {
            double minX = body.x - RADIUS;
            active.removeIf(candidate -> (candidate.x + RADIUS) < minX);
            active.add(body);
            peak = Math.max(peak, active.size());
        }
        return peak;
    }

    private Aabb aabbOf(MovingSphere body) {
        return new Aabb(
                body.x - RADIUS, body.y - RADIUS, body.z - RADIUS,
                body.x + RADIUS, body.y + RADIUS, body.z + RADIUS);
    }

    private org.dynamisfx.collision.BoundingSphere sphereOf(MovingSphere body) {
        return new org.dynamisfx.collision.BoundingSphere(body.x, body.y, body.z, RADIUS);
    }

    private Distribution selectedDistribution() {
        return distributionBox == null || distributionBox.getValue() == null
                ? Distribution.UNIFORM
                : distributionBox.getValue();
    }

    private static double ms(long nanos) {
        return Math.round(nanos / 10000.0) / 100.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double randomRange(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static Box wireCube(double size, Color color) {
        Box box = new Box(size, size, size);
        box.setDrawMode(DrawMode.LINE);
        box.setCullFace(CullFace.NONE);
        box.setMaterial(new PhongMaterial(color));
        return box;
    }

    private enum Distribution {
        UNIFORM,
        CLUSTERED,
        PLANAR
    }

    private static final class MovingSphere {
        private double x;
        private double y;
        private double z;
        private double vx;
        private double vy;
        private double vz;
        private final Sphere left;
        private final Sphere right;

        private MovingSphere(
                double x, double y, double z,
                double vx, double vy, double vz,
                Sphere left, Sphere right) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.left = left;
            this.right = right;
        }
    }
}
