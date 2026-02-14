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
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.dynamisfx.physics.api.PhysicsBodyHandle;
import org.dynamisfx.physics.api.PhysicsConstraintHandle;
import org.dynamisfx.physics.model.PhysicsVector3;

/**
 * Spinning windmill that knocks objects around.
 * Demonstrates continuous motor rotation and collision interactions.
 *
 * Controls:
 * +/- - Adjust windmill speed
 * D - Drop objects
 * S - Stop/start windmill
 */
public class Ode4jWindmillDemo extends Ode4jDemoBase {

    private static final double BLADE_LENGTH = 120;
    private static final double BLADE_WIDTH = 20;
    private static final double BLADE_DEPTH = 8;

    private PhysicsConstraintHandle windmillMotor;
    private double windmillSpeed = 3.0;
    private boolean windmillRunning = true;

    private Slider speedSlider;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void setupWorld() {
        // Ground
        createFloor(-250, 600, 400, Color.DARKGREEN);

        // ====== WINDMILL ======
        // Tower
        PhysicsBodyHandle tower = createStaticBox(0, -100, 0, 40, 200, 40, Color.SADDLEBROWN);

        // Hub (center of blades)
        PhysicsBodyHandle hub = createBox(0, 50, 30, 30, 30, 20, 5.0, Color.DIMGRAY);

        // Windmill motor (hinge on Z-axis)
        windmillMotor = createHingeJoint(tower, hub,
                new PhysicsVector3(0, 50, 20),
                new PhysicsVector3(0, 0, 1),
                null, null);  // No limits - free rotation

        enableMotor(windmillMotor, windmillSpeed, 200.0);

        // Blades (4 blades attached to hub)
        PhysicsBodyHandle blade1 = createBox(0, 50 + BLADE_LENGTH / 2, 40, BLADE_WIDTH, BLADE_LENGTH, BLADE_DEPTH, 1.0, Color.WHITESMOKE);
        createFixedJoint(hub, blade1);

        PhysicsBodyHandle blade2 = createBox(0, 50 - BLADE_LENGTH / 2, 40, BLADE_WIDTH, BLADE_LENGTH, BLADE_DEPTH, 1.0, Color.WHITESMOKE);
        createFixedJoint(hub, blade2);

        PhysicsBodyHandle blade3 = createBox(BLADE_LENGTH / 2, 50, 40, BLADE_LENGTH, BLADE_WIDTH, BLADE_DEPTH, 1.0, Color.WHITESMOKE);
        createFixedJoint(hub, blade3);

        PhysicsBodyHandle blade4 = createBox(-BLADE_LENGTH / 2, 50, 40, BLADE_LENGTH, BLADE_WIDTH, BLADE_DEPTH, 1.0, Color.WHITESMOKE);
        createFixedJoint(hub, blade4);

        // ====== OBJECTS TO KNOCK AROUND ======
        // Ramp to funnel objects toward windmill
        createStaticBox(-150, -150, 50, 100, 10, 60, Color.DARKGRAY);

        // Initial objects
        dropObjects(5);
    }

    private void dropObjects(int count) {
        for (int i = 0; i < count; i++) {
            double x = -200 + Math.random() * 100;
            double y = 100 + Math.random() * 100;
            double z = 30 + (Math.random() - 0.5) * 40;
            Color color = Color.hsb(Math.random() * 360, 0.7, 0.8);

            if (Math.random() > 0.5) {
                createSphere(x, y, z, 12 + Math.random() * 8, 0.5, color);
            } else {
                createBox(x, y, z, 20, 20, 20, 0.8, color);
            }
        }
    }

    @Override
    protected void handleKeyPressed(KeyCode code) {
        switch (code) {
            case D -> dropObjects(3);
            case S -> toggleWindmill();
            case EQUALS, PLUS -> adjustSpeed(0.5);
            case MINUS -> adjustSpeed(-0.5);
            default -> { }
        }
    }

    private void toggleWindmill() {
        windmillRunning = !windmillRunning;
        if (windmillRunning) {
            enableMotor(windmillMotor, windmillSpeed, 200.0);
        } else {
            disableMotor(windmillMotor);
        }
    }

    private void adjustSpeed(double delta) {
        windmillSpeed = Math.max(-10, Math.min(10, windmillSpeed + delta));
        if (speedSlider != null) {
            speedSlider.setValue(windmillSpeed);
        }
        if (windmillRunning) {
            setMotorVelocity(windmillMotor, windmillSpeed);
        }
    }

    @Override
    protected Node buildDemoControlPanel() {
        VBox panel = new VBox(6);

        Button dropBtn = new Button("Drop Objects (D)");
        dropBtn.setOnAction(e -> dropObjects(3));

        Button toggleBtn = new Button("Toggle Motor (S)");
        toggleBtn.setOnAction(e -> toggleWindmill());

        Label speedLabel = new Label("Windmill Speed:");
        speedSlider = new Slider(-10, 10, windmillSpeed);
        speedSlider.setPrefWidth(120);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(5);
        speedSlider.valueProperty().addListener((obs, oldV, newV) -> {
            windmillSpeed = newV.doubleValue();
            if (windmillRunning) {
                setMotorVelocity(windmillMotor, windmillSpeed);
            }
        });

        panel.getChildren().addAll(
                new Label("Windmill Controls:"),
                new Label(""),
                dropBtn,
                toggleBtn,
                new Label(""),
                speedLabel,
                speedSlider,
                new Label(""),
                new Label("SPACE - Pause/Resume"),
                new Label(""),
                new Label("Watch objects get"),
                new Label("knocked around!")
        );

        return panel;
    }

    @Override
    public String getSampleDescription() {
        return "Spinning windmill that knocks objects around. Adjust speed with slider.";
    }
}
