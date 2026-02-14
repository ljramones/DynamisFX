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
 * Demonstrates a motorized crane with:
 * - Rotating base (hinge motor)
 * - Extending arm (slider motor)
 * - Hanging hook (ball joint chain)
 *
 * Controls:
 * LEFT/RIGHT - Rotate crane
 * UP/DOWN - Extend/retract arm
 */
public class Ode4jCraneDemo extends Ode4jDemoBase {

    private PhysicsConstraintHandle rotationMotor;
    private PhysicsConstraintHandle extensionMotor;
    private PhysicsBodyHandle hook;

    private static final double ROTATION_SPEED = 1.5;  // rad/s
    private static final double EXTENSION_SPEED = 50.0; // units/s
    private static final double ROTATION_TORQUE = 500.0;
    private static final double EXTENSION_FORCE = 300.0;

    private boolean rotatingLeft = false;
    private boolean rotatingRight = false;
    private boolean extending = false;
    private boolean retracting = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Floor with cargo area
        createFloor(-250, 800, 600, Color.DARKSLATEGRAY);

        // Create some cargo boxes to pick up
        createBox(-200, -210, 100, 50, 50, 50, 2.0, Color.ORANGE);
        createBox(-100, -210, -100, 40, 40, 40, 1.5, Color.GOLD);
        createBox(200, -210, 50, 60, 60, 60, 3.0, Color.CORAL);

        // ====== CRANE BASE ======
        // Static foundation
        PhysicsBodyHandle foundation = createStaticBox(0, -180, 0, 100, 60, 100, Color.DIMGRAY);

        // Rotating base (turntable)
        PhysicsBodyHandle turntable = createBox(0, -130, 0, 80, 20, 80, 10.0, Color.SLATEGRAY);

        // Rotation motor (vertical axis)
        rotationMotor = createHingeJoint(
                foundation, turntable,
                new PhysicsVector3(0, -140, 0),
                new PhysicsVector3(0, 1, 0),  // Y-axis rotation
                null, null);

        // ====== CRANE ARM ======
        // Vertical mast
        PhysicsBodyHandle mast = createBox(0, -30, 0, 30, 180, 30, 5.0, Color.DARKGRAY);
        createFixedJoint(turntable, mast);

        // Horizontal arm base (fixed to mast)
        PhysicsBodyHandle armBase = createBox(0, 60, 0, 40, 30, 40, 3.0, Color.STEELBLUE);
        createFixedJoint(mast, armBase);

        // Extending arm section
        PhysicsBodyHandle extendingArm = createBox(80, 60, 0, 120, 20, 30, 4.0, Color.ROYALBLUE);

        // Extension slider
        extensionMotor = createSliderJoint(
                armBase, extendingArm,
                new PhysicsVector3(1, 0, 0),  // X-axis extension
                0.0, 150.0);  // Limits: can extend up to 150 units

        // ====== HOOK ASSEMBLY ======
        // Cable connection point
        PhysicsBodyHandle cableAttach = createSphere(140, 50, 0, 10, 0.5, Color.GRAY);
        createBallJoint(extendingArm, cableAttach, new PhysicsVector3(140, 55, 0));

        // Cable segments (simplified as spheres connected by ball joints)
        PhysicsBodyHandle cable1 = createSphere(140, 20, 0, 8, 0.3, Color.DARKGRAY);
        createBallJoint(cableAttach, cable1, new PhysicsVector3(140, 35, 0));

        PhysicsBodyHandle cable2 = createSphere(140, -10, 0, 8, 0.3, Color.DARKGRAY);
        createBallJoint(cable1, cable2, new PhysicsVector3(140, 5, 0));

        // Hook
        hook = createBox(140, -40, 0, 30, 20, 30, 2.0, Color.YELLOW);
        createBallJoint(cable2, hook, new PhysicsVector3(140, -25, 0));
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
                case LEFT -> {
                    rotatingLeft = true;
                    updateRotationMotor();
                }
                case RIGHT -> {
                    rotatingRight = true;
                    updateRotationMotor();
                }
                case UP -> {
                    extending = true;
                    updateExtensionMotor();
                }
                case DOWN -> {
                    retracting = true;
                    updateExtensionMotor();
                }
                default -> { }
            }
        });

        subScene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT -> {
                    rotatingLeft = false;
                    updateRotationMotor();
                }
                case RIGHT -> {
                    rotatingRight = false;
                    updateRotationMotor();
                }
                case UP -> {
                    extending = false;
                    updateExtensionMotor();
                }
                case DOWN -> {
                    retracting = false;
                    updateExtensionMotor();
                }
                default -> { }
            }
        });
    }

    private void updateRotationMotor() {
        double velocity = 0;
        if (rotatingLeft && !rotatingRight) {
            velocity = ROTATION_SPEED;
        } else if (rotatingRight && !rotatingLeft) {
            velocity = -ROTATION_SPEED;
        }

        if (velocity != 0) {
            enableMotor(rotationMotor, velocity, ROTATION_TORQUE);
        } else {
            // Hold position by using a small max force
            enableMotor(rotationMotor, 0, ROTATION_TORQUE * 0.5);
        }
    }

    private void updateExtensionMotor() {
        double velocity = 0;
        if (extending && !retracting) {
            velocity = EXTENSION_SPEED;
        } else if (retracting && !extending) {
            velocity = -EXTENSION_SPEED;
        }

        if (velocity != 0) {
            enableMotor(extensionMotor, velocity, EXTENSION_FORCE);
        } else {
            // Hold position
            enableMotor(extensionMotor, 0, EXTENSION_FORCE);
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        panel.getChildren().addAll(
                new Label("Crane Controls:"),
                new Label(""),
                new Label("LEFT/RIGHT - Rotate crane"),
                new Label("UP/DOWN - Extend/retract arm"),
                new Label(""),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Try to pick up the"),
                new Label("colored cargo boxes!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Motorized crane with rotating base and extending arm. Use arrow keys to control.";
    }
}
