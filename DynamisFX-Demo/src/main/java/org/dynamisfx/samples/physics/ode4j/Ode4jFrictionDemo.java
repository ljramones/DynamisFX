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

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.model.PhysicsQuaternion;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates friction effects on sliding objects.
 * A tilted ramp with boxes that slide at different rates based on friction.
 * Press P to push all boxes.
 * Based on ODE4j DemoFriction.
 */
public class Ode4jFrictionDemo extends Ode4jDemoBase {

    private static final double BOX_SIZE = 40;
    private static final double RAMP_WIDTH = 600;
    private static final double RAMP_LENGTH = 400;
    private static final double RAMP_ANGLE = 15; // degrees
    private static final int NUM_BOXES = 5;

    private PhysicsBodyHandle[] boxes;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Create a tilted ramp
        // The ramp is rotated around the Z axis
        PhysicsQuaternion rampRotation = quaternionFromAxisAngle(0, 0, 1, RAMP_ANGLE);

        // Create the ramp as a rotated static box
        // Position it so boxes can slide down
        createStaticRamp(-50, -100, 0, RAMP_WIDTH, 30, RAMP_LENGTH, Color.SLATEGRAY, rampRotation);

        // Create floor at the bottom to catch sliding boxes
        createFloor(-300, 800, 800, Color.DIMGRAY);

        // Create boxes at the top of the ramp
        // Each box is positioned side by side
        boxes = new PhysicsBodyHandle[NUM_BOXES];
        double startX = -150;
        double startY = 50;
        double spacing = RAMP_LENGTH / (NUM_BOXES + 1);

        Color[] colors = {
                Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE
        };

        for (int i = 0; i < NUM_BOXES; i++) {
            double z = -RAMP_LENGTH / 2 + spacing * (i + 1);
            boxes[i] = createBox(startX, startY, z, BOX_SIZE, BOX_SIZE, BOX_SIZE, 1.0, colors[i]);
        }
    }

    private void createStaticRamp(double x, double y, double z,
                                  double w, double h, double d,
                                  Color color, PhysicsQuaternion orientation) {
        javafx.scene.shape.Box box = new javafx.scene.shape.Box(w, h, d);
        box.setMaterial(new javafx.scene.paint.PhongMaterial(color));
        worldGroup.getChildren().add(box);

        org.dynamisfx.physics.api.PhysicsBodyHandle handle = world.createBody(
                new org.dynamisfx.physics.model.PhysicsBodyDefinition(
                        org.dynamisfx.physics.model.PhysicsBodyType.STATIC,
                        0.0,
                        new org.dynamisfx.physics.model.BoxShape(w, h, d),
                        new org.dynamisfx.physics.model.PhysicsBodyState(
                                new PhysicsVector3(x, y, z),
                                orientation,
                                PhysicsVector3.ZERO,
                                PhysicsVector3.ZERO,
                                org.dynamisfx.physics.model.ReferenceFrame.WORLD,
                                0.0)));
        sceneSync.bind(handle, box);
        bodyNodes.put(handle, box);
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        if (code == KeyCode.P) {
            pushAllBoxes();
        }
    }

    private void pushAllBoxes() {
        if (boxes != null) {
            for (PhysicsBodyHandle box : boxes) {
                if (box != null) {
                    // Push boxes down the ramp
                    applyImpulse(box, new PhysicsVector3(50, 0, 0));
                }
            }
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(8);
        panel.getChildren().addAll(
                new Label("Press P - Push all boxes"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Adjust Friction slider to see"),
                new Label("how boxes slide differently."),
                new Label(""),
                new Label("Lower friction = faster sliding"),
                new Label("Higher friction = slower/no sliding")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates friction effects on a tilted ramp. Adjust friction to see different sliding behavior.";
    }
}
