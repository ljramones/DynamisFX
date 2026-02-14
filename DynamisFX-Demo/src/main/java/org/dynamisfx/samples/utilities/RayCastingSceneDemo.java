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
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import org.dynamisfx.collision.Aabb;
import org.dynamisfx.collision.BroadPhase3D;
import org.dynamisfx.collision.CollisionPair;
import org.dynamisfx.collision.Intersection3D;
import org.dynamisfx.collision.Ray3D;
import org.dynamisfx.collision.SpatialHash3D;
import org.dynamisfx.collision.SweepAndPrune3D;
import org.dynamisfx.samples.shapes.ShapeBaseSample;

/**
 * Ray casting demo with brute-force and broad-phase accelerated modes.
 */
public class RayCastingSceneDemo extends ShapeBaseSample<Group> {

    private static final double FAR = 900.0;

    private final Group world = new Group();
    private final Group beamLayer = new Group();
    private final List<Target> targets = new ArrayList<>();
    private final Object proxy = new Object();

    private Slider countSlider;
    private Slider originX;
    private Slider originY;
    private Slider originZ;
    private Slider yaw;
    private Slider pitch;
    private CheckBox flashlight;
    private CheckBox accelerated;
    private ComboBox<String> broadPhaseBox;
    private Label stats = new Label();

    private AnimationTimer timer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void createMesh() {
        camera.setTranslateZ(-1200);
        world.getChildren().clear();
        beamLayer.getChildren().clear();
        world.getChildren().add(beamLayer);
        buildTargets(120);
        model = world;
    }

    @Override
    protected void addMeshAndListeners() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                castAndRender();
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
        root.getChildren().add(new Label("Ray Casting Scene"));

        countSlider = slider(40, 450, 120);
        countSlider.valueProperty().addListener((o, a, b) -> buildTargets(b.intValue()));

        originX = slider(-450, 450, -300);
        originY = slider(-250, 250, 0);
        originZ = slider(-250, 250, 0);
        yaw = slider(-180, 180, 0);
        pitch = slider(-80, 80, 0);

        flashlight = new CheckBox("Flashlight mode (64 rays)");
        accelerated = new CheckBox("Accelerated candidates");
        accelerated.setSelected(true);

        broadPhaseBox = new ComboBox<>();
        broadPhaseBox.getItems().setAll("SpatialHash3D", "SweepAndPrune3D");
        broadPhaseBox.setValue("SpatialHash3D");

        root.getChildren().addAll(
                new Label("Object count"), countSlider,
                new Label("Origin X/Y/Z"), originX, originY, originZ,
                new Label("Yaw / Pitch"), yaw, pitch,
                flashlight,
                accelerated,
                new Label("Broad phase"), broadPhaseBox,
                stats);
        return root;
    }

    private void buildTargets(int count) {
        world.getChildren().removeIf(node -> node instanceof Box || node instanceof Sphere);
        targets.clear();
        for (int i = 0; i < count; i++) {
            boolean sphere = (i % 4) == 0;
            Node node;
            if (sphere) {
                Sphere s = new Sphere(10 + (i % 6) * 2);
                s.setMaterial(new PhongMaterial(Color.DARKSEAGREEN));
                node = s;
            } else {
                Box b = new Box(18 + (i % 5) * 6, 18 + (i % 7) * 4, 18 + (i % 3) * 8);
                b.setMaterial(new PhongMaterial(Color.SLATEBLUE));
                node = b;
            }
            double x = ((i * 53) % 800) - 400;
            double y = ((i * 37) % 400) - 200;
            double z = ((i * 29) % 320) - 160;
            node.setTranslateX(x);
            node.setTranslateY(y);
            node.setTranslateZ(z);
            world.getChildren().add(node);
            targets.add(new Target(node));
        }
    }

    private void castAndRender() {
        resetColors();
        beamLayer.getChildren().clear();

        double ox = value(originX, -300);
        double oy = value(originY, 0);
        double oz = value(originZ, 0);
        List<Ray3D> rays = buildRays(ox, oy, oz);

        int totalTests = 0;
        int totalHits = 0;

        for (Ray3D ray : rays) {
            List<Hit> hits = computeHits(ray);
            totalTests += lastTestCount;
            totalHits += hits.size();
            drawRay(ray, hits);
            for (Hit hit : hits) {
                hit.target.highlight();
            }
        }

        stats.setText("rays=" + rays.size() + " tests=" + totalTests + " hits=" + totalHits);
    }

    private int lastTestCount;

    private List<Hit> computeHits(Ray3D ray) {
        List<Target> candidates = accelerated != null && accelerated.isSelected()
                ? broadPhaseCandidates(ray)
                : new ArrayList<>(targets);
        lastTestCount = candidates.size();

        List<Hit> hits = new ArrayList<>();
        for (Target target : candidates) {
            OptionalDouble distance = Intersection3D.rayAabbIntersectionDistance(ray, target.aabb());
            if (distance.isPresent()) {
                hits.add(new Hit(target, distance.getAsDouble()));
            }
        }
        hits.sort(Comparator.comparingDouble(h -> h.distance));
        return hits;
    }

    private List<Target> broadPhaseCandidates(Ray3D ray) {
        Aabb rayAabb = rayAabb(ray);
        List<Object> items = new ArrayList<>(targets.size() + 1);
        items.add(proxy);
        items.addAll(targets);

        BroadPhase3D<Object> broad = "SweepAndPrune3D".equals(broadPhaseBox.getValue())
                ? new SweepAndPrune3D<>()
                : new SpatialHash3D<>(120.0);

        Set<CollisionPair<Object>> pairs = broad.findPotentialPairs(items, item -> {
            if (item == proxy) {
                return rayAabb;
            }
            return ((Target) item).aabb();
        });

        List<Target> candidates = new ArrayList<>();
        for (CollisionPair<Object> pair : pairs) {
            if (pair.first() == proxy && pair.second() instanceof Target t) {
                candidates.add(t);
            } else if (pair.second() == proxy && pair.first() instanceof Target t) {
                candidates.add(t);
            }
        }
        return candidates;
    }

    private static Aabb rayAabb(Ray3D ray) {
        double ex = ray.originX() + ray.dirX() * FAR;
        double ey = ray.originY() + ray.dirY() * FAR;
        double ez = ray.originZ() + ray.dirZ() * FAR;
        return new Aabb(
                Math.min(ray.originX(), ex),
                Math.min(ray.originY(), ey),
                Math.min(ray.originZ(), ez),
                Math.max(ray.originX(), ex),
                Math.max(ray.originY(), ey),
                Math.max(ray.originZ(), ez));
    }

    private List<Ray3D> buildRays(double ox, double oy, double oz) {
        List<Ray3D> rays = new ArrayList<>();
        if (flashlight != null && flashlight.isSelected()) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    double dyaw = (i - 3.5) * 2.2;
                    double dpitch = (j - 3.5) * 1.7;
                    rays.add(makeRay(ox, oy, oz, value(yaw, 0) + dyaw, value(pitch, 0) + dpitch));
                }
            }
        } else {
            rays.add(makeRay(ox, oy, oz, value(yaw, 0), value(pitch, 0)));
        }
        return rays;
    }

    private static Ray3D makeRay(double ox, double oy, double oz, double yawDeg, double pitchDeg) {
        double yaw = Math.toRadians(yawDeg);
        double pitch = Math.toRadians(pitchDeg);
        double dx = Math.cos(pitch) * Math.cos(yaw);
        double dy = Math.sin(pitch);
        double dz = Math.cos(pitch) * Math.sin(yaw);
        return new Ray3D(ox, oy, oz, dx, dy, dz);
    }

    private void drawRay(Ray3D ray, List<Hit> hits) {
        Color color = hits.isEmpty() ? Color.LIGHTGRAY : Color.GOLD;
        for (int i = 0; i < 28; i++) {
            double t = FAR * i / 27.0;
            Sphere sample = new Sphere(1.5);
            sample.setMaterial(new PhongMaterial(color));
            sample.setTranslateX(ray.originX() + ray.dirX() * t);
            sample.setTranslateY(ray.originY() + ray.dirY() * t);
            sample.setTranslateZ(ray.originZ() + ray.dirZ() * t);
            beamLayer.getChildren().add(sample);
        }

        int index = 0;
        for (Hit hit : hits) {
            Sphere marker = new Sphere(4.0);
            marker.setMaterial(new PhongMaterial(index % 2 == 0 ? Color.LIMEGREEN : Color.ORANGERED));
            marker.setTranslateX(ray.originX() + ray.dirX() * hit.distance);
            marker.setTranslateY(ray.originY() + ray.dirY() * hit.distance);
            marker.setTranslateZ(ray.originZ() + ray.dirZ() * hit.distance);
            beamLayer.getChildren().add(marker);
            index++;
            if (index >= 6) {
                break;
            }
        }
    }

    private void resetColors() {
        for (Target target : targets) {
            target.reset();
        }
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

    private static final class Hit {
        private final Target target;
        private final double distance;

        private Hit(Target target, double distance) {
            this.target = target;
            this.distance = distance;
        }
    }

    private static final class Target {
        private final Node node;

        private Target(Node node) {
            this.node = node;
        }

        private Aabb aabb() {
            return Aabb.fromBounds(node.getBoundsInParent());
        }

        private void highlight() {
            if (node instanceof Box box) {
                box.setMaterial(new PhongMaterial(Color.GOLD));
            } else if (node instanceof Sphere sphere) {
                sphere.setMaterial(new PhongMaterial(Color.GOLD));
            }
        }

        private void reset() {
            if (node instanceof Box box) {
                box.setMaterial(new PhongMaterial(Color.SLATEBLUE));
            } else if (node instanceof Sphere sphere) {
                sphere.setMaterial(new PhongMaterial(Color.DARKSEAGREEN));
            }
        }
    }
}
