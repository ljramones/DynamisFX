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
 * Demonstrates slider joints (prismatic joints) with linear limits.
 * Use LEFT/RIGHT arrow keys to push the piston.
 * Based on ODE4j DemoSlider.
 */
public class Ode4jSliderDemo extends Ode4jDemoBase {

    private static final double PISTON_LENGTH = 80;
    private static final double PISTON_SIZE = 50;
    private static final double CYLINDER_LENGTH = 400;
    private static final double CYLINDER_SIZE = 70;
    private static final double PUSH_FORCE = 100;

    // Slider limits
    private static final double LO_STOP = -150;
    private static final double HI_STOP = 150;

    private PhysicsBodyHandle pistonBody;
    private PhysicsBodyHandle cylinderBody;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Create floor
        createFloor(-250, 800, 800, Color.DIMGRAY);

        // Create the cylinder housing (static)
        cylinderBody = createStaticBox(
                0, 0, 0,
                CYLINDER_LENGTH, CYLINDER_SIZE, CYLINDER_SIZE,
                Color.SLATEGRAY);

        // Create the piston (dynamic)
        pistonBody = createBox(
                0, 0, 0,
                PISTON_LENGTH, PISTON_SIZE - 10, PISTON_SIZE - 10,
                2.0, Color.CRIMSON);

        // Create slider joint along X axis
        PhysicsVector3 axis = new PhysicsVector3(1, 0, 0);
        createSliderJoint(cylinderBody, pistonBody, axis, LO_STOP, HI_STOP);

        // Create a second slider (vertical, no limits) for demonstration
        PhysicsBodyHandle rail = createStaticBox(
                250, 100, 0,
                30, 300, 30,
                Color.DARKGRAY);

        PhysicsBodyHandle slider2 = createBox(
                250, 50, 0,
                60, 40, 60,
                1.5, Color.FORESTGREEN);

        PhysicsVector3 verticalAxis = new PhysicsVector3(0, 1, 0);
        createSliderJoint(rail, slider2, verticalAxis, -100.0, 100.0);
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case LEFT -> pushPiston(-PUSH_FORCE);
            case RIGHT -> pushPiston(PUSH_FORCE);
            case UP -> applyImpulse(pistonBody, new PhysicsVector3(0, 50, 0));
            default -> { }
        }
    }

    private void pushPiston(double force) {
        if (pistonBody != null) {
            applyImpulse(pistonBody, new PhysicsVector3(force, 0, 0));
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(8);
        panel.getChildren().addAll(
                new Label("LEFT/RIGHT - Push piston"),
                new Label("UP - Apply upward force"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Red piston: Horizontal slider"),
                new Label("Green box: Vertical slider")
        );
        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Demonstrates slider joints with linear limits. Use arrow keys to push.";
    }
}
