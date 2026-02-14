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
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Demonstrates a simple 4-wheeled buggy with motor-driven wheels.
 *
 * Controls:
 * W/UP - Drive forward
 * S/DOWN - Drive backward
 * A/LEFT - Turn left
 * D/RIGHT - Turn right
 */
public class Ode4jBuggyDemo extends Ode4jDemoBase {

    // Buggy dimensions
    private static final double CHASSIS_WIDTH = 80;
    private static final double CHASSIS_HEIGHT = 20;
    private static final double CHASSIS_LENGTH = 120;
    private static final double WHEEL_RADIUS = 25;
    private static final double WHEEL_WIDTH = 15;
    private static final double WHEEL_OFFSET_X = 50;  // From center
    private static final double WHEEL_OFFSET_Z = 45;  // From center

    // Motor parameters
    private static final double DRIVE_SPEED = 10.0;   // rad/s
    private static final double DRIVE_TORQUE = 50.0;
    private static final double TURN_SPEED = 5.0;     // rad/s for turning

    private PhysicsBodyHandle chassis;
    private PhysicsConstraintHandle[] wheelMotors = new PhysicsConstraintHandle[4];

    // Control state
    private boolean accelerating = false;
    private boolean reversing = false;
    private boolean turningLeft = false;
    private boolean turningRight = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Large floor area
        createFloor(-250, 1000, 1000, Color.DARKGREEN);

        // Create some obstacles
        createStaticBox(-300, -200, 200, 60, 60, 60, Color.SADDLEBROWN);
        createStaticBox(200, -200, -300, 80, 80, 80, Color.SADDLEBROWN);
        createStaticBox(-100, -200, -400, 50, 100, 50, Color.SADDLEBROWN);
        createStaticBox(350, -200, 100, 70, 50, 70, Color.SADDLEBROWN);

        // Ramp
        PhysicsBodyHandle ramp = createStaticBox(0, -220, 300, 150, 20, 100, Color.DARKGRAY);
        // Note: Ramp rotation would require quaternion orientation support

        // ====== BUILD THE BUGGY ======
        double chassisY = -180;

        // Chassis body
        chassis = createBox(0, chassisY, 0, CHASSIS_WIDTH, CHASSIS_HEIGHT, CHASSIS_LENGTH, 5.0, Color.CRIMSON);

        // Create 4 wheels with hinge motors
        // Front-left wheel
        PhysicsBodyHandle wheelFL = createWheelBody(-WHEEL_OFFSET_X, chassisY, WHEEL_OFFSET_Z);
        wheelMotors[0] = createWheelJoint(chassis, wheelFL, -WHEEL_OFFSET_X, chassisY, WHEEL_OFFSET_Z);

        // Front-right wheel
        PhysicsBodyHandle wheelFR = createWheelBody(WHEEL_OFFSET_X, chassisY, WHEEL_OFFSET_Z);
        wheelMotors[1] = createWheelJoint(chassis, wheelFR, WHEEL_OFFSET_X, chassisY, WHEEL_OFFSET_Z);

        // Rear-left wheel
        PhysicsBodyHandle wheelRL = createWheelBody(-WHEEL_OFFSET_X, chassisY, -WHEEL_OFFSET_Z);
        wheelMotors[2] = createWheelJoint(chassis, wheelRL, -WHEEL_OFFSET_X, chassisY, -WHEEL_OFFSET_Z);

        // Rear-right wheel
        PhysicsBodyHandle wheelRR = createWheelBody(WHEEL_OFFSET_X, chassisY, -WHEEL_OFFSET_Z);
        wheelMotors[3] = createWheelJoint(chassis, wheelRR, WHEEL_OFFSET_X, chassisY, -WHEEL_OFFSET_Z);
    }

    private PhysicsBodyHandle createWheelBody(double x, double y, double z) {
        // Using a sphere for the wheel physics (simplification)
        return createSphere(x, y, z, WHEEL_RADIUS, 0.5, Color.DARKSLATEGRAY);
    }

    private PhysicsConstraintHandle createWheelJoint(PhysicsBodyHandle chassis,
                                                      PhysicsBodyHandle wheel,
                                                      double x, double y, double z) {
        // Hinge joint on X-axis (perpendicular to travel direction)
        return createHingeJoint(
                chassis, wheel,
                new PhysicsVector3(x, y, z),
                new PhysicsVector3(1, 0, 0),  // X-axis rotation for wheels
                null, null);
    }

    @Override
    protected void setupKeyboardControls() {
        super.setupKeyboardControls();

        subScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                paused = !paused;
                if (pauseCheckBox != null) {
                    pauseCheckBox.setSelected(paused);
                }
                event.consume();
                return;
            }

            switch (event.getCode()) {
                case W, UP -> {
                    accelerating = true;
                    updateWheelMotors();
                }
                case S, DOWN -> {
                    reversing = true;
                    updateWheelMotors();
                }
                case A, LEFT -> {
                    turningLeft = true;
                    updateWheelMotors();
                }
                case D, RIGHT -> {
                    turningRight = true;
                    updateWheelMotors();
                }
                case R -> resetBuggy();
                default -> { }
            }
        });

        subScene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case W, UP -> {
                    accelerating = false;
                    updateWheelMotors();
                }
                case S, DOWN -> {
                    reversing = false;
                    updateWheelMotors();
                }
                case A, LEFT -> {
                    turningLeft = false;
                    updateWheelMotors();
                }
                case D, RIGHT -> {
                    turningRight = false;
                    updateWheelMotors();
                }
                default -> { }
            }
        });
    }

    private void updateWheelMotors() {
        // Calculate base speed
        double speed = 0;
        if (accelerating && !reversing) {
            speed = -DRIVE_SPEED;  // Negative for forward motion
        } else if (reversing && !accelerating) {
            speed = DRIVE_SPEED;
        }

        // Apply differential for turning
        double leftSpeed = speed;
        double rightSpeed = speed;

        if (turningLeft && !turningRight) {
            // Slow down left wheels, speed up right
            leftSpeed = speed * 0.3;
            rightSpeed = speed + TURN_SPEED;
        } else if (turningRight && !turningLeft) {
            // Speed up left wheels, slow down right
            leftSpeed = speed + TURN_SPEED;
            rightSpeed = speed * 0.3;
        }

        // Apply to all wheels
        // Left wheels (indices 0, 2)
        for (int i : new int[]{0, 2}) {
            if (leftSpeed != 0 || speed != 0) {
                enableMotor(wheelMotors[i], leftSpeed, DRIVE_TORQUE);
            } else {
                disableMotor(wheelMotors[i]);
            }
        }

        // Right wheels (indices 1, 3)
        for (int i : new int[]{1, 3}) {
            if (rightSpeed != 0 || speed != 0) {
                enableMotor(wheelMotors[i], rightSpeed, DRIVE_TORQUE);
            } else {
                disableMotor(wheelMotors[i]);
            }
        }
    }

    private void resetBuggy() {
        resetWorld();
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        panel.getChildren().addAll(
                new Label("Buggy Controls:"),
                new Label(""),
                new Label("W/UP - Drive forward"),
                new Label("S/DOWN - Drive backward"),
                new Label("A/LEFT - Turn left"),
                new Label("D/RIGHT - Turn right"),
                new Label("R - Reset buggy"),
                new Label(""),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Drive around the"),
                new Label("obstacles!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "4-wheeled buggy with motor-driven wheels. Use WASD or arrow keys to drive.";
    }
}
