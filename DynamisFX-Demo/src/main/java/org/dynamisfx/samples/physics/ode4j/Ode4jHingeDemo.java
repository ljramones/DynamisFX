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
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates hinge joints with angular limits.
 * Use LEFT/RIGHT arrow keys to apply torque to the door.
 * Based on ODE4j DemoHinge.
 */
public class Ode4jHingeDemo extends Ode4jDemoBase {

    private static final double DOOR_WIDTH = 150;
    private static final double DOOR_HEIGHT = 200;
    private static final double DOOR_DEPTH = 15;
    private static final double FRAME_SIZE = 20;
    private static final double TORQUE_STRENGTH = 50;

    // Hinge limits in radians (-90 to +90 degrees)
    private static final double LO_STOP = -Math.PI / 2;
    private static final double HI_STOP = Math.PI / 2;

    private PhysicsBodyHandle doorBody;
    private PhysicsBodyHandle frameBody;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Create floor
        createFloor(-250, 800, 800, Color.DIMGRAY);

        // Create door frame (static)
        frameBody = createStaticBox(
                -DOOR_WIDTH / 2 - FRAME_SIZE / 2, 0, 0,
                FRAME_SIZE, DOOR_HEIGHT + 2 * FRAME_SIZE, FRAME_SIZE,
                Color.SADDLEBROWN);

        // Create door (dynamic)
        doorBody = createBox(
                0, 0, 0,
                DOOR_WIDTH, DOOR_HEIGHT, DOOR_DEPTH,
                2.0, Color.BURLYWOOD);

        // Create hinge joint along the left edge of the door
        PhysicsVector3 anchor = new PhysicsVector3(-DOOR_WIDTH / 2, 0, 0);
        PhysicsVector3 axis = new PhysicsVector3(0, 1, 0); // Vertical axis
        createHingeJoint(frameBody, doorBody, anchor, axis, LO_STOP, HI_STOP);

        // Create a second door for comparison (no limits)
        PhysicsBodyHandle frame2 = createStaticBox(
                300 - DOOR_WIDTH / 2 - FRAME_SIZE / 2, 0, 0,
                FRAME_SIZE, DOOR_HEIGHT + 2 * FRAME_SIZE, FRAME_SIZE,
                Color.SADDLEBROWN);

        PhysicsBodyHandle door2 = createBox(
                300, 0, 0,
                DOOR_WIDTH, DOOR_HEIGHT, DOOR_DEPTH,
                2.0, Color.DARKORANGE);

        // Hinge without limits
        PhysicsVector3 anchor2 = new PhysicsVector3(300 - DOOR_WIDTH / 2, 0, 0);
        createHingeJoint(frame2, door2, anchor2, axis, null, null);
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case LEFT -> applyTorque(-TORQUE_STRENGTH);
            case RIGHT -> applyTorque(TORQUE_STRENGTH);
            default -> { }
        }
    }

    private void applyTorque(double torque) {
        if (doorBody != null) {
            // Apply angular velocity change around Y axis
            setAngularVelocity(doorBody, new PhysicsVector3(0, torque, 0));
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(8);
        panel.getChildren().addAll(
                new Label("LEFT/RIGHT - Apply torque to door"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Left door: Limited (-90 to +90 deg)"),
                new Label("Right door: No limits")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates hinge joints with angular limits. Use arrow keys to rotate door.";
    }
}
