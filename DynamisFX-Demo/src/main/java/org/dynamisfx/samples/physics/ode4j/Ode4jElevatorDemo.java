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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsBodyState;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Multi-floor elevator platform using slider motor.
 * Objects can ride the elevator between floors.
 *
 * Controls:
 * 1/2/3 - Go to floor 1/2/3
 * D - Drop object on elevator
 * UP/DOWN - Manual control
 */
public class Ode4jElevatorDemo extends Ode4jDemoBase {

    private static final double FLOOR_HEIGHT = 120;
    private static final double PLATFORM_WIDTH = 100;
    private static final double PLATFORM_DEPTH = 80;
    private static final double SHAFT_HEIGHT = 400;

    private PhysicsConstraintHandle elevatorMotor;
    private PhysicsBodyHandle platform;
    private int currentFloor = 1;
    private int targetFloor = 1;

    private Label floorLabel;

    // Floor Y positions (relative to shaft base)
    private static final double[] FLOOR_POSITIONS = {0, FLOOR_HEIGHT, FLOOR_HEIGHT * 2};

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        currentFloor = 1;
        targetFloor = 1;

        // Ground
        createFloor(-250, 600, 500, Color.DARKSLATEGRAY);

        // ====== ELEVATOR SHAFT ======
        // Back wall
        createStaticBox(0, -50, -PLATFORM_DEPTH / 2 - 10, PLATFORM_WIDTH + 40, SHAFT_HEIGHT, 20, Color.DIMGRAY);

        // Side walls
        createStaticBox(-PLATFORM_WIDTH / 2 - 20, -50, 0, 20, SHAFT_HEIGHT, PLATFORM_DEPTH + 20, Color.DIMGRAY);
        createStaticBox(PLATFORM_WIDTH / 2 + 20, -50, 0, 20, SHAFT_HEIGHT, PLATFORM_DEPTH + 20, Color.DIMGRAY);

        // ====== ELEVATOR PLATFORM ======
        // Shaft guide (static, invisible anchor)
        PhysicsBodyHandle guide = createStaticBox(0, 0, 0, 10, 10, 10, Color.TRANSPARENT);

        // Platform
        platform = createBox(0, -210, 0, PLATFORM_WIDTH, 15, PLATFORM_DEPTH, 10.0, Color.GOLDENROD);

        // Slider joint for vertical movement
        elevatorMotor = createSliderJoint(guide, platform,
                new PhysicsVector3(0, 1, 0),  // Y-axis sliding
                -230.0, FLOOR_HEIGHT * 2 + 50);

        // Start at floor 1
        enableMotor(elevatorMotor, 0, 500.0);  // Hold position

        // ====== FLOOR PLATFORMS (for loading/unloading) ======
        // Floor 1 loading platform
        createStaticBox(-PLATFORM_WIDTH - 30, -220, 0, 60, 10, PLATFORM_DEPTH, Color.FORESTGREEN);
        createStaticBox(PLATFORM_WIDTH + 30, -220, 0, 60, 10, PLATFORM_DEPTH, Color.FORESTGREEN);

        // Floor 2 loading platform
        createStaticBox(-PLATFORM_WIDTH - 30, -220 + FLOOR_HEIGHT, 0, 60, 10, PLATFORM_DEPTH, Color.STEELBLUE);
        createStaticBox(PLATFORM_WIDTH + 30, -220 + FLOOR_HEIGHT, 0, 60, 10, PLATFORM_DEPTH, Color.STEELBLUE);

        // Floor 3 loading platform
        createStaticBox(-PLATFORM_WIDTH - 30, -220 + FLOOR_HEIGHT * 2, 0, 60, 10, PLATFORM_DEPTH, Color.CORAL);
        createStaticBox(PLATFORM_WIDTH + 30, -220 + FLOOR_HEIGHT * 2, 0, 60, 10, PLATFORM_DEPTH, Color.CORAL);

        // Initial cargo
        createBox(0, -190, 0, 30, 30, 30, 1.0, Color.ORANGE);
    }

    private void goToFloor(int floor) {
        if (floor < 1 || floor > 3) return;
        targetFloor = floor;

        // Calculate direction and speed
        PhysicsBodyState state = world.getBodyState(platform);
        double currentY = state.position().y();
        double targetY = -210 + FLOOR_POSITIONS[floor - 1];

        double speed = 80.0;  // Units per second
        if (targetY < currentY) {
            speed = -speed;  // Go down
        }

        enableMotor(elevatorMotor, speed, 500.0);
        updateFloorLabel();
    }

    private void dropObject() {
        PhysicsBodyState state = world.getBodyState(platform);
        double y = state.position().y() + 50;

        Color color = Color.hsb(Math.random() * 360, 0.7, 0.8);
        if (Math.random() > 0.5) {
            createBox((Math.random() - 0.5) * 40, y, (Math.random() - 0.5) * 30,
                    25, 25, 25, 1.0, color);
        } else {
            createSphere((Math.random() - 0.5) * 40, y, (Math.random() - 0.5) * 30,
                    15, 0.8, color);
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case DIGIT1, NUMPAD1 -> goToFloor(1);
            case DIGIT2, NUMPAD2 -> goToFloor(2);
            case DIGIT3, NUMPAD3 -> goToFloor(3);
            case D -> dropObject();
            case UP -> enableMotor(elevatorMotor, 60.0, 500.0);
            case DOWN -> enableMotor(elevatorMotor, -60.0, 500.0);
            default -> { }
        }
    }

    @Override
    protected void setupKeyboardControls() {
        super.setupKeyboardControls();

        subScene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                // Stop when key released
                enableMotor(elevatorMotor, 0, 500.0);
            }
        });
    }

    private void updateFloorLabel() {
        if (floorLabel != null) {
            floorLabel.setText("Target: Floor " + targetFloor);
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        floorLabel = new Label("Target: Floor 1");

        HBox floorButtons = new HBox(4);
        for (int i = 1; i <= 3; i++) {
            final int floor = i;
            Button btn = new Button("Floor " + i);
            btn.setOnAction(e -> goToFloor(floor));
            floorButtons.getChildren().add(btn);
        }

        Button dropBtn = new Button("Drop Object (D)");
        dropBtn.setOnAction(e -> dropObject());

        panel.getChildren().addAll(
                new Label("Elevator Controls:"),
                new Label(""),
                floorLabel,
                floorButtons,
                new Label(""),
                dropBtn,
                new Label(""),
                new Label("UP/DOWN - Manual"),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Transport objects"),
                new Label("between floors!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Multi-floor elevator with slider motor. Press 1/2/3 to select floor.";
    }
}
