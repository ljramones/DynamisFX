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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsVector3;
import org.dynamisfx.physics.model.PhysicsWorldConfiguration;
import org.dynamisfx.physics.model.ReferenceFrame;

/**
 * Pinball table with motor-driven flippers.
 *
 * Controls:
 * A/LEFT - Left flipper
 * D/RIGHT - Right flipper
 * SPACE - Launch new ball
 */
public class Ode4jPinballDemo extends Ode4jDemoBase {

    private static final double TABLE_WIDTH = 300;
    private static final double TABLE_HEIGHT = 500;
    private static final double FLIPPER_LENGTH = 60;
    private static final double FLIPPER_WIDTH = 15;
    private static final double BALL_RADIUS = 12;

    private PhysicsConstraintHandle leftFlipper;
    private PhysicsConstraintHandle rightFlipper;
    private PhysicsBodyHandle ball;

    private boolean leftFlipperActive = false;
    private boolean rightFlipperActive = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected PhysicsWorldConfiguration defaultConfiguration() {
        // Tilted table - gravity pulls ball "down" the table (toward +Z in physics coords)
        return new PhysicsWorldConfiguration(
                ReferenceFrame.WORLD,
                new PhysicsVector3(0, -5, 3),  // Angled gravity
                1.0 / 120.0);
    }

    @Override
    protected void setupWorld() {
        // Table surface (tilted back)
        createStaticBox(0, -20, 0, TABLE_WIDTH, 20, TABLE_HEIGHT, Color.DARKGREEN);

        // Side walls
        createStaticBox(-TABLE_WIDTH / 2 - 10, 20, 0, 20, 60, TABLE_HEIGHT, Color.SADDLEBROWN);
        createStaticBox(TABLE_WIDTH / 2 + 10, 20, 0, 20, 60, TABLE_HEIGHT, Color.SADDLEBROWN);

        // Top wall
        createStaticBox(0, 20, -TABLE_HEIGHT / 2 - 10, TABLE_WIDTH, 60, 20, Color.SADDLEBROWN);

        // Bottom walls (with gap for drain)
        createStaticBox(-100, 20, TABLE_HEIGHT / 2 + 10, 100, 60, 20, Color.SADDLEBROWN);
        createStaticBox(100, 20, TABLE_HEIGHT / 2 + 10, 100, 60, 20, Color.SADDLEBROWN);

        // Bumpers (cylindrical approximated with spheres)
        createStaticBumper(-60, 30, -100, Color.RED);
        createStaticBumper(60, 30, -100, Color.BLUE);
        createStaticBumper(0, 30, -50, Color.YELLOW);
        createStaticBumper(-80, 30, 0, Color.MAGENTA);
        createStaticBumper(80, 30, 0, Color.CYAN);

        // Flippers
        createFlippers();

        // Launch new ball
        launchBall();
    }

    private void createStaticBumper(double x, double y, double z, Color color) {
        // Using sphere as bumper
        PhysicsBodyHandle bumper = createStaticBox(x, y, z, 30, 40, 30, color);
    }

    private void createFlippers() {
        // Left flipper base (static)
        PhysicsBodyHandle leftBase = createStaticBox(-70, 10, 150, 20, 20, 20, Color.DIMGRAY);

        // Left flipper paddle
        PhysicsBodyHandle leftPaddle = createBox(-40, 10, 150, FLIPPER_LENGTH, 10, FLIPPER_WIDTH, 2.0, Color.ORANGE);

        // Left flipper hinge
        leftFlipper = createHingeJoint(leftBase, leftPaddle,
                new PhysicsVector3(-70, 10, 150),
                new PhysicsVector3(0, 1, 0),  // Y-axis rotation
                -0.5, 0.5);  // Limited rotation

        // Right flipper base (static)
        PhysicsBodyHandle rightBase = createStaticBox(70, 10, 150, 20, 20, 20, Color.DIMGRAY);

        // Right flipper paddle
        PhysicsBodyHandle rightPaddle = createBox(40, 10, 150, FLIPPER_LENGTH, 10, FLIPPER_WIDTH, 2.0, Color.ORANGE);

        // Right flipper hinge
        rightFlipper = createHingeJoint(rightBase, rightPaddle,
                new PhysicsVector3(70, 10, 150),
                new PhysicsVector3(0, 1, 0),  // Y-axis rotation
                -0.5, 0.5);  // Limited rotation
    }

    private void launchBall() {
        // Launch from top of table
        ball = createSphere(0, 30, -200, BALL_RADIUS, 0.5, Color.SILVER);
        // Give initial velocity
        applyImpulse(ball, new PhysicsVector3((Math.random() - 0.5) * 50, 0, 100));
    }

    @Override
    protected void setupKeyboardControls() {
        super.setupKeyboardControls();

        subScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case SPACE -> launchBall();
                case A, LEFT -> {
                    leftFlipperActive = true;
                    activateLeftFlipper();
                }
                case D, RIGHT -> {
                    rightFlipperActive = true;
                    activateRightFlipper();
                }
                default -> { }
            }
        });

        subScene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case A, LEFT -> {
                    leftFlipperActive = false;
                    deactivateLeftFlipper();
                }
                case D, RIGHT -> {
                    rightFlipperActive = false;
                    deactivateRightFlipper();
                }
                default -> { }
            }
        });
    }

    private void activateLeftFlipper() {
        enableMotor(leftFlipper, 20.0, 100.0);  // Swing up
    }

    private void deactivateLeftFlipper() {
        enableMotor(leftFlipper, -10.0, 50.0);  // Return down
    }

    private void activateRightFlipper() {
        enableMotor(rightFlipper, -20.0, 100.0);  // Swing up (opposite direction)
    }

    private void deactivateRightFlipper() {
        enableMotor(rightFlipper, 10.0, 50.0);  // Return down
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        Button launchBtn = new Button("Launch Ball (SPACE)");
        launchBtn.setOnAction(e -> launchBall());

        panel.getChildren().addAll(
                new Label("Pinball Controls:"),
                new Label(""),
                new Label("A/LEFT - Left flipper"),
                new Label("D/RIGHT - Right flipper"),
                launchBtn,
                new Label(""),
                new Label("Keep the ball in play!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Pinball table with motor-driven flippers. Use A/D to flip!";
    }
}
