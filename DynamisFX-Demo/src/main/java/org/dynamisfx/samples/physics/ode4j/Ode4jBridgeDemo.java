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

package org.dynamisfx.samples.physics.ode4j;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates a suspension bridge made of planks connected by hinge joints.
 * Press O to drop objects onto the bridge.
 * Press J to make objects jump across.
 */
public class Ode4jBridgeDemo extends Ode4jDemoBase {

    private static final double PLANK_WIDTH = 60;
    private static final double PLANK_HEIGHT = 10;
    private static final double PLANK_DEPTH = 80;
    private static final double PLANK_MASS = 2.0;
    private static final int DEFAULT_PLANKS = 12;

    private static final double TOWER_WIDTH = 40;
    private static final double TOWER_HEIGHT = 200;

    private List<PhysicsBodyHandle> planks = new ArrayList<>();
    private List<PhysicsBodyHandle> droppedObjects = new ArrayList<>();
    private Slider plankCountSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        planks.clear();

        // Create a chasm with floor far below
        createFloor(-400, 1200, 800, Color.DARKSLATEGRAY);

        int plankCount = plankCountSlider != null ? (int) plankCountSlider.getValue() : DEFAULT_PLANKS;
        double bridgeLength = plankCount * PLANK_WIDTH;
        double startX = -bridgeLength / 2;

        // Create towers on each side
        PhysicsBodyHandle leftTower = createStaticBox(
                startX - TOWER_WIDTH / 2 - PLANK_WIDTH / 2, -150, 0,
                TOWER_WIDTH, TOWER_HEIGHT, PLANK_DEPTH + 20,
                Color.SADDLEBROWN);

        PhysicsBodyHandle rightTower = createStaticBox(
                -startX + TOWER_WIDTH / 2 + PLANK_WIDTH / 2, -150, 0,
                TOWER_WIDTH, TOWER_HEIGHT, PLANK_DEPTH + 20,
                Color.SADDLEBROWN);

        // Build the bridge planks
        double y = -50; // Bridge height
        PhysicsBodyHandle prevBody = leftTower;
        double prevX = startX - PLANK_WIDTH;

        for (int i = 0; i < plankCount; i++) {
            double x = startX + i * PLANK_WIDTH;
            Color color = (i % 2 == 0) ? Color.BURLYWOOD : Color.TAN;

            PhysicsBodyHandle plank = createBox(x, y, 0,
                    PLANK_WIDTH - 4, PLANK_HEIGHT, PLANK_DEPTH,
                    PLANK_MASS, color);
            planks.add(plank);

            // Connect to previous plank/tower with hinge joint
            // Hinge axis is along Z (allows planks to swing up/down)
            PhysicsVector3 anchor = new PhysicsVector3((prevX + x) / 2 + PLANK_WIDTH / 2, y, 0);
            PhysicsVector3 axis = new PhysicsVector3(0, 0, 1);

            // Limited rotation to prevent planks from flipping
            createHingeJoint(prevBody, plank, anchor, axis, -Math.PI / 6, Math.PI / 6);

            prevBody = plank;
            prevX = x;
        }

        // Connect last plank to right tower
        PhysicsVector3 rightAnchor = new PhysicsVector3(
                -startX - PLANK_WIDTH / 2, y, 0);
        PhysicsVector3 axis = new PhysicsVector3(0, 0, 1);
        createHingeJoint(prevBody, rightTower, rightAnchor, axis, -Math.PI / 6, Math.PI / 6);

        // Add rope railings (chain of small spheres)
        addRailing(plankCount, startX, y + 40, -PLANK_DEPTH / 2 - 5);
        addRailing(plankCount, startX, y + 40, PLANK_DEPTH / 2 + 5);
    }

    private void addRailing(int segments, double startX, double y, double z) {
        PhysicsBodyHandle prev = null;

        for (int i = 0; i <= segments; i++) {
            double x = startX + i * PLANK_WIDTH - PLANK_WIDTH / 2;
            PhysicsBodyHandle node = createSphere(x, y, z, 5, 0.1, Color.SIENNA);

            if (prev != null) {
                PhysicsVector3 anchor = new PhysicsVector3((x + startX + (i - 1) * PLANK_WIDTH - PLANK_WIDTH / 2) / 2, y, z);
                createBallJoint(prev, node, anchor);
            } else {
                // Anchor first node to tower
                createBallJoint(
                        bodyNodes.keySet().stream().findFirst().orElse(null),
                        node,
                        new PhysicsVector3(x, y, z));
            }
            prev = node;
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case O -> dropObject();
            case J -> launchJumper();
            case C -> clearObjects();
            default -> { }
        }
    }

    private void dropObject() {
        // Drop a sphere onto the middle of the bridge
        double x = (Math.random() - 0.5) * 200;
        PhysicsBodyHandle obj = createSphere(x, 150, 0, 20, 2.0, Color.CRIMSON);
        droppedObjects.add(obj);
    }

    private void launchJumper() {
        // Launch a box from one side to try to cross the bridge
        PhysicsBodyHandle jumper = createBox(-400, 0, 0, 30, 30, 30, 3.0, Color.FORESTGREEN);
        applyImpulse(jumper, new PhysicsVector3(300, 100, 0));
        droppedObjects.add(jumper);
    }

    private void clearObjects() {
        for (PhysicsBodyHandle obj : droppedObjects) {
            world.removeBody(obj);
            Node node = bodyNodes.remove(obj);
            if (node != null) {
                worldGroup.getChildren().remove(node);
            }
            sceneSync.unbindHandle(obj);
        }
        droppedObjects.clear();
    }

    @Override
    protected void resetWorld() {
        if (timer != null) {
            timer.stop();
        }
        if (world != null) {
            world.close();
        }
        bodyNodes.clear();
        if (sceneSync != null) {
            sceneSync.clear();
        }
        worldGroup.getChildren().clear();
        planks.clear();
        droppedObjects.clear();

        world = backend.createWorld(defaultConfiguration());
        accumulator = new org.dynamisfx.physics.step.FixedStepAccumulator(1.0 / 120.0, 8);

        setupWorld();

        timer.start();
        syncControlsFromWorld();
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(8);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        plankCountSlider = addSlider(grid, 0, "Planks", 6, 20, DEFAULT_PLANKS);
        plankCountSlider.setMajorTickUnit(2);
        plankCountSlider.setSnapToTicks(true);

        panel.getChildren().addAll(
                grid,
                new Label(""),
                new Label("Press O - Drop object"),
                new Label("Press J - Launch jumper"),
                new Label("Press C - Clear objects"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Watch the bridge sway"),
                new Label("under the weight!")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates a suspension bridge. Press O to drop objects onto it.";
    }
}
